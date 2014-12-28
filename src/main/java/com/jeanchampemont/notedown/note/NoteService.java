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
import org.springframework.beans.factory.annotation.Autowired;
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

    @Transactional(readOnly = true)
    public Iterable<Note> getAll() {
        User user = authenticationService.getCurrentUser();
        return getAll(user);
    }

    @Transactional(readOnly = true)
    public Iterable<Note> getAll(User user) {
        return repo.findByUserOrderByLastModificationDesc(user);
    }

    @Transactional(readOnly = true)
    public Note get(UUID id) {
        return repo.findOne(id);
    }

    @Transactional
    public Note save(Note note) {
        User user = authenticationService.getCurrentUser();
        return save(user, note);
    }

    @Transactional
    public Note save(User user, Note note) {
        note = updateLastModification(note);
        note.setUser(user);
        return repo.save(note);
    }

    @Transactional
    public void delete(UUID id) {
        repo.delete(id);
    }

    private Note updateLastModification(Note note) {
        note.setLastModification(new Date());
        return note;
    }
}
