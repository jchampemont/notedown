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

import com.jeanchampemont.notedown.note.NoteService;
import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    private NoteService noteService;

    private UserService userService;

    @Autowired
    public ApplicationStartup(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent e) {
        User user = userService.create("admin@world.com", "admin");
        userService.setLocale(user, "fr");
        noteService.save(user, new Note("Test 1", "BLA BLA BLA"));
        noteService.save(user, new Note("Test 2", "BLA BLA BLA"));
        noteService.save(user, new Note("Test 3", "BLA BLA BLA"));
        noteService.save(user, new Note("Test 4", "BLA BLA BLA"));
    }
}
