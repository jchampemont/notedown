/*
 * Copyright (C) 2014, 2015 NoteDown
 *
 * This file is part of the NoteDown project.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.jeanchampemont.notedown.note;

import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.note.persistence.NoteEvent;
import com.jeanchampemont.notedown.note.persistence.repository.NoteEventRepository;
import com.jeanchampemont.notedown.note.persistence.repository.NoteRepository;
import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.utils.exception.OperationNotAllowedException;
import difflib.PatchFailedException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class NoteService {

    private Log log = LogFactory.getLog(NoteService.class);

    private NoteRepository repo;

    private NoteEventRepository eventRepo;

    private AuthenticationService authenticationService;

    private int maxHistorySize;

    @Autowired
    public NoteService(NoteRepository repo, NoteEventRepository eventRepo
            , AuthenticationService authenticationService
            , @Value("${notedown.max-history-size}") int maxHistorySize) {
        this.repo = repo;
        this.eventRepo = eventRepo;
        this.authenticationService = authenticationService;
        this.maxHistorySize = maxHistorySize;
    }

    /**
     * @return all notes for the current user
     */
    @Transactional(readOnly = true)
    public Iterable<Note> getNotes() {
        User user = authenticationService.getCurrentUser();
        return repo.findByUserOrderByLastModificationDesc(user);
    }

    /**
     * Get a not by ID.
     *
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    @Cacheable("note")
    public Note get(UUID id) {
        User user = authenticationService.getCurrentUser();
        Note note = repo.findOne(id);
        if (note == null) {
            return null;
        }
        if (hasReadAccess(user, note)) {
            return note;
        } else {
            throw new OperationNotAllowedException();
        }
    }

    /**
     * Create or update a note
     *
     * @param note
     * @return
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @CacheEvict(value = "note", key = "#note.id")
    public Note createUpdate(Note note, Long version) {
        User user = authenticationService.getCurrentUser();
        Note originalNote = repo.findOne(note.getId());
        String originalContent;
        if (originalNote == null) {
            originalNote = note;
            originalContent = "";
        } else {
            originalContent = originalNote.getContent();
        }
        if (hasWriteAccess(user, originalNote)) {
            originalNote = updateLastModification(originalNote);
            originalNote.setTitle(note.getTitle());
            originalNote.setContent(note.getContent());
            originalNote.setUser(user);
            originalNote = repo.save(originalNote);

            NoteEvent event = NoteEventHelper.builder()
                    .noteId(originalNote.getId())
                    .version(version + 1)
                    .user(user)
                    .title(note.getTitle())
                    .diff(originalContent, note.getContent())
                    .save()
                    .build();

            event.setNote(originalNote);
            event = eventRepo.save(event);

            originalNote.getEvents().add(event);

            return originalNote;
        } else {
            throw new OperationNotAllowedException();
        }
    }

    /**
     * Delete a note by id
     *
     * @param id
     */
    @Transactional
    @CacheEvict(value = "note")
    public void delete(UUID id) {
        User user = authenticationService.getCurrentUser();
        Note originalNote = repo.findOne(id);
        if (originalNote != null) {
            if (hasWriteAccess(user, originalNote)) {
                repo.delete(id);
            } else {
                throw new OperationNotAllowedException();
            }
        }
    }

    @Transactional
    @Scheduled(fixedDelay = 2 * 1000 * 60 * 60, initialDelay = 2 * 1000 * 60 * 60)
    public void compressHistory() {
        log.info("Starting note compression...");
        StopWatch stopWatch = new StopWatch("compressHistory");
        stopWatch.start();
        Iterable<Note> notes = repo.findAll();
        for (Note note : notes) {
            if (note.getEvents().size() > maxHistorySize) {
                log.debug("compressing note " + note.getId());
                List<NoteEvent> keptEvents = note.getEvents().subList(0, maxHistorySize - 1);

                Note copy = new Note();
                copy.setContent(note.getContent());
                copy.setId(note.getId());
                copy.setTitle(note.getTitle());

                try {
                    copy = NoteEventHelper.unPatch(keptEvents, copy);
                } catch (PatchFailedException e) {
                    log.error("Unpatching NoteEvent should not fail here...");
                }
                String content = copy.getContent();

                List<NoteEvent> oldEvents = note.getEvents().subList(maxHistorySize - 1, note.getEvents().size());
                try {
                    copy = NoteEventHelper.unPatch(oldEvents, copy);
                } catch (PatchFailedException e) {
                    log.error("Unpatching NoteEvent should not fail here...");
                }
                NoteEvent compressedEvent = NoteEventHelper.builder()
                        .noteId(note.getId())
                        .version(oldEvents.get(0).getId().getVersion())
                        .user(note.getUser())
                        .title(oldEvents.get(0).getTitle())
                        .diff(copy.getContent(), content)
                        .compress()
                        .build();
                eventRepo.delete(oldEvents);

                keptEvents.add(compressedEvent);
                eventRepo.save(compressedEvent);
            }
        }
        stopWatch.stop();
        log.info("Finished note compression");
        log.info(stopWatch.shortSummary());
    }

    private Note updateLastModification(Note note) {
        note.setLastModification(new Date());
        return note;
    }

    private boolean hasReadAccess(User user, Note note) {
        return note.getUser() == null || note.getUser().getId() == user.getId();
    }

    private boolean hasWriteAccess(User user, Note note) {
        return note.getUser() == null || note.getUser().getId() == user.getId();
    }
}
