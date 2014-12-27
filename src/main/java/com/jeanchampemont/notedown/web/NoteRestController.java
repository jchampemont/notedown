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
package com.jeanchampemont.notedown.web;

import com.jeanchampemont.notedown.note.NoteService;
import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.web.api.NoteDto;
import com.jeanchampemont.notedown.web.utils.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/note")
public class NoteRestController {

    private NoteService noteService;

    @Autowired
    public NoteRestController(NoteService noteService) {
        this.noteService = noteService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public NoteDto getById(@PathVariable("id") UUID id) {
        Note note = noteService.get(id);
        if(note == null) {
            throw new ResourceNotFoundException();
        }
        NoteDto result = new NoteDto();
        result.setId(note.getId().toString());
        result.setTitle(note.getTitle());
        result.setContent(note.getContent());
        return result;
    }

}
