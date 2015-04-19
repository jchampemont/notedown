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
package com.jeanchampemont.notedown.web;

import com.jeanchampemont.notedown.note.NoteService;
import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.security.AuthenticationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@Controller
@RequestMapping("/app")
public class NoteController {

    private NoteService noteService;

    private AuthenticationService authenticationService;

    @Autowired
    public NoteController(NoteService noteService, AuthenticationService authenticationService) {
        this.noteService = noteService;
        this.authenticationService = authenticationService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap model) {
        model.put("notes", noteService.getNotes());
        return "list";
    }

    @RequestMapping(value = "/note/new", method = RequestMethod.GET)
    public String editNew(ModelMap model) {
        model.put("note", new Note());
        return "edit";
    }

    @RequestMapping(value = "/note/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") UUID id, ModelMap model) {
        model.put("note", noteService.get(id));
        return "edit";
    }

    @RequestMapping(value = "/note/delete/{id}", method = RequestMethod.GET)
    public String delete(@PathVariable("id") UUID id, ModelMap model) {
        noteService.delete(id);
        return "redirect:/app";
    }
}
