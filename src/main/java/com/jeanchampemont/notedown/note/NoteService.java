/*
 * Copyright (C) 2014 NoteDown
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
import com.jeanchampemont.notedown.note.persistence.repository.NoteRepository;
import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.utils.exception.OperationNotAllowedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
public class NoteService {

    private NoteRepository repo;

    private UserService userService;

    private AuthenticationService authenticationService;

    @Autowired
    public NoteService(NoteRepository repo, UserService userService, AuthenticationService authenticationService) {
        this.repo = repo;
        this.userService = userService;
        this.authenticationService = authenticationService;
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
     * @param id
     * @return
     */
    @Transactional(readOnly = true)
    @Cacheable("note")
    public Note get(UUID id) {
        User user = authenticationService.getCurrentUser();
        Note note = repo.findOne(id);
        if(note == null) {
            return null;
        }
        if(hasReadAccess(user, note)) {
            return note;
        } else {
            throw new OperationNotAllowedException();
        }
    }

    /**
     * Create or update a note
     * @param note
     * @return
     */
    @Transactional
    @CacheEvict(value = "note", key = "#note.id")
    public Note createUpdate(Note note) {
        User user = authenticationService.getCurrentUser();
        Note originalNote = repo.findOne(note.getId());
        if(originalNote == null) {
            originalNote = note;
        }
        if(hasWriteAccess(user, originalNote)) {
            originalNote = updateLastModification(originalNote);
            originalNote.setTitle(note.getTitle());
            originalNote.setContent(note.getContent());
            originalNote.setUser(user);
            return repo.save(originalNote);
        } else {
            throw new OperationNotAllowedException();
        }
    }

    /**
     * Delete a note by id
     * @param id
     */
    @Transactional
    @CacheEvict(value = "note")
    public void delete(UUID id) {
        User user = authenticationService.getCurrentUser();
        Note originalNote = repo.findOne(id);
        if(originalNote != null) {
            if (hasWriteAccess(user, originalNote)) {
                repo.delete(id);
            } else {
                throw new OperationNotAllowedException();
            }
        }
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
