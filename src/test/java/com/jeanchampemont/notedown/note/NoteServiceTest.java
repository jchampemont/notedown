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

import com.jeanchampemont.notedown.NoteDownApplication;
import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.note.persistence.repository.NoteRepository;
import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.utils.exception.OperationNotAllowedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NoteDownApplication.class)
public class NoteServiceTest {
    private NoteService sut;

    private NoteRepository repoMock;

    private UserService userServiceMock;

    private AuthenticationService authenticationServiceMock;

    @Before
    public void init() {
        repoMock = mock(NoteRepository.class);
        userServiceMock = mock(UserService.class);
        authenticationServiceMock = mock(AuthenticationService.class);
        sut = new NoteService(repoMock, userServiceMock, authenticationServiceMock);
    }

    @Test
    public void testGetNotes() {
        User user = new User();

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findByUserOrderByLastModificationDesc(user)).thenReturn(Collections.emptyList());

        Iterable<Note> result = sut.getNotes();

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findByUserOrderByLastModificationDesc(user);

        assertFalse(result.iterator().hasNext());
    }

    @Test
    public void testGet() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(12);

        Note note = new Note();
        note.setUser(user);

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findOne(id)).thenReturn(note);

        Note result = sut.get(id);

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(id);

        assertEquals(note, result);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void testGetNotAllowed() {
        UUID id = UUID.randomUUID();

        User user = new User();
        user.setId(12);

        Note note = new Note();
        note.setUser(user);

        User unauthorizedUser = new User();
        unauthorizedUser.setId(23);

        when(authenticationServiceMock.getCurrentUser()).thenReturn(unauthorizedUser);
        when(repoMock.findOne(id)).thenReturn(note);

        sut.get(id);

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(id);
    }

    @Test
    public void testCreateUpdateNew() {
        User user = new User();
        user.setId(12);

        Note note = new Note();

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findOne(note.getId())).thenReturn(null);
        when(repoMock.save(note)).thenReturn(note);

        Note result = sut.createUpdate(note);

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(note.getId());
        verify(repoMock).save(note);

        assertEquals(result, note);
    }

    @Test
    public void testCreateUpdateExisting() {
        User user = new User();
        user.setId(12);

        Note existingNote = new Note();

        Note note = new Note();
        note.setId(existingNote.getId());
        note.setTitle("title");
        note.setContent("content");

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findOne(note.getId())).thenReturn(existingNote);
        when(repoMock.save(existingNote)).thenReturn(existingNote);

        Note result = sut.createUpdate(note);

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(note.getId());
        verify(repoMock).save(existingNote);

        assertEquals(result, existingNote);
    }

    @Test(expected = OperationNotAllowedException.class)
    public void testCreateUpdateNotAllowed() {
        User user = new User();
        user.setId(12);

        User notAllowedUser = new User();
        notAllowedUser.setId(122);

        Note existingNote = new Note();
        existingNote.setUser(user);

        Note note = new Note();
        note.setId(existingNote.getId());
        note.setTitle("title");
        note.setContent("content");

        when(authenticationServiceMock.getCurrentUser()).thenReturn(notAllowedUser);
        when(repoMock.findOne(note.getId())).thenReturn(existingNote);

        sut.createUpdate(note);

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(note.getId());
    }

    @Test
    public void testDelete() {
        User user = new User();
        user.setId(12);

        Note note = new Note();

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findOne(note.getId())).thenReturn(note);

        sut.delete(note.getId());

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(note.getId());
        verify(repoMock).delete(note.getId());
    }

}
