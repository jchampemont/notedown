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

import com.jeanchampemont.notedown.note.dto.NoteDto;
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
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StopWatch;

import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

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
    public Iterable<NoteDto> getNotes() {
        User user = authenticationService.getCurrentUser();
        return repo.findByUserOrderByLastModificationDesc(user).stream().map(note -> mapNoteToNoteDto(note)).collect(Collectors.toList());
    }

    /**
     * Get a not by ID.
     *
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    public NoteDto get(UUID id) {
        User user = authenticationService.getCurrentUser();
        Note note = repo.findOne(id);
        if (note == null) {
            return null;
        }
        if (hasReadAccess(user, note)) {
            return mapNoteToNoteDto(note);
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
    public NoteDto createUpdate(NoteDto note, Long version) {
        User user = authenticationService.getCurrentUser();
        Note dbNote = repo.findOne(note.getIdAsUUID());
        if (dbNote == null) {
            dbNote = new Note();
            dbNote.setTitle("");
            dbNote.setContent("");
        }
        if (hasWriteAccess(user, dbNote)) {
            //Perform save only if the note has changed
            if (!note.getContent().equals(dbNote.getContent()) || !note.getTitle().equals(dbNote.getTitle())) {
                NoteEvent event = NoteEventHelper.builder()
                        .noteId(dbNote.getId())
                        .version(version + 1)
                        .user(user)
                        .title(note.getTitle())
                        .diff(dbNote.getContent(), note.getContent())
                        .save()
                        .build();

                dbNote = updateLastModification(dbNote);
                dbNote.setTitle(note.getTitle());
                dbNote.setContent(note.getContent());
                dbNote.setUser(user);
                dbNote = repo.save(dbNote);

                event.setNote(dbNote);
                event = eventRepo.save(event);

                dbNote.getEvents().add(0, event);
            }
            return mapNoteToNoteDto(dbNote);
        } else {
            throw new OperationNotAllowedException();
        }
    }

    /**
     * Check if version available on client side is too old to
     * get saved
     *
     * @param noteId
     * @param version
     * @return
     */
    @Transactional(readOnly = true)
    public boolean isVersionOutdated(UUID noteId, Long version) {
        Note originalNote = repo.findOne(noteId);

        return originalNote != null && version < originalNote.getFirstAvailableVersion();
    }

    /**
     * Delete a note by id
     *
     * @param id
     */
    @Transactional
    public void delete(String id) {
        User user = authenticationService.getCurrentUser();
        Note originalNote = repo.findOne(UUID.fromString(id));
        if (originalNote != null) {
            if (hasWriteAccess(user, originalNote)) {
                repo.delete(UUID.fromString(id));
            } else {
                throw new OperationNotAllowedException();
            }
        }
    }

    @Transactional
    @Scheduled(cron = "${notedown.history-compress-cron}")
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

    private NoteDto mapNoteToNoteDto(Note n) {
        NoteDto result = new NoteDto();
        result.setId(n.getId().toString());
        result.setTitle(n.getTitle());
        result.setContent(n.getContent());
        result.setVersion(n.getLastVersion());
        result.setLastModification(n.getLastModification());
        result.setLastVersion(n.getLastVersion());
        return result;
    }
}
