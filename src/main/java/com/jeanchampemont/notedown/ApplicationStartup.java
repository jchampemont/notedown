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
package com.jeanchampemont.notedown;

import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.note.persistence.repository.NoteRepository;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    private NoteRepository noteRepository;

    private UserService userService;

    @Autowired
    public ApplicationStartup(UserService userService, NoteRepository noteRepository) {
        this.noteRepository = noteRepository;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent e) {
        //Init with some sample Data
        User user = new User();
        user.setEmail("admin@world.com");
        user.setPassword("admin");
        user.setLocale("fr");
        user = userService.create(user);
        Note note = new Note("Test 1", "BLA BLA BLA", user);
        note.setLastModification(new Date());
        noteRepository.save(note);
        note = new Note("Test 2", "BLA BLA BLA", user);
        note.setLastModification(new Date());
        noteRepository.save(note);
        note = new Note("Test 3", "BLA BLA BLA", user);
        note.setLastModification(new Date());
        noteRepository.save(note);
        note = new Note("Test 4", "BLA BLA BLA", user);
        note.setLastModification(new Date());
        noteRepository.save(note);
    }
}
