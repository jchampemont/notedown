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

import com.jeanchampemont.notedown.NoteDownApplication;
import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.note.persistence.NoteEvent;
import com.jeanchampemont.notedown.note.persistence.repository.NoteEventRepository;
import com.jeanchampemont.notedown.note.persistence.repository.NoteRepository;
import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.utils.exception.OperationNotAllowedException;
import com.jeanchampemont.notedown.web.api.NoteDto;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.mockito.AdditionalAnswers.returnsFirstArg;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NoteDownApplication.class)
public class NoteServiceTest {
    private NoteService sut;

    private NoteRepository repoMock;

    private NoteEventRepository eventRepoMock;

    private AuthenticationService authenticationServiceMock;

    @Before
    public void init() {
        repoMock = mock(NoteRepository.class);
        authenticationServiceMock = mock(AuthenticationService.class);
        eventRepoMock = mock(NoteEventRepository.class);
        sut = new NoteService(repoMock, eventRepoMock, authenticationServiceMock, 10);
    }

    @Test
    public void testGetNotes() {
        User user = new User();

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findByUserOrderByLastModificationDesc(user)).thenReturn(Collections.emptyList());

        Iterable<NoteDto> result = sut.getNotes();

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
        note.setTitle("My Title");
        note.setContent("My Content");

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findOne(id)).thenReturn(note);

        NoteDto result = sut.get(id);

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(id);

        assertEquals(note.getTitle(), result.getTitle());
        assertEquals(note.getContent(), result.getContent());
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
    }

    @Test
    public void testCreateUpdateNew() {
        User user = new User();
        user.setId(12);

        Note note = new Note();
        note.setContent("crazy new conTent");
        note.setTitle("new title");

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findOne(note.getId())).thenReturn(null);
        when(repoMock.save(any(Note.class))).thenAnswer(returnsFirstArg());
        when(eventRepoMock.save(any(NoteEvent.class))).thenAnswer(returnsFirstArg());

        NoteDto result = sut.createUpdate(mapNoteToNoteDto(note), 0L);

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(note.getId());
        ArgumentCaptor<NoteEvent> argument = ArgumentCaptor.forClass(NoteEvent.class);
        verify(eventRepoMock).save(argument.capture());

        assertEquals(new Long(1L), argument.getValue().getId().getVersion());
        assertEquals(user, argument.getValue().getUser());
        assertEquals(note.getTitle(), result.getTitle());
        assertEquals(note.getContent(), result.getContent());
    }

    @Test
    public void testCreateUpdateExisting() {
        User user = new User();
        user.setId(12);

        Note existingNote = new Note();
        existingNote.setContent("old content");

        Note note = new Note();
        note.setId(existingNote.getId());
        note.setTitle("title");
        note.setContent("content");

        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.findOne(note.getId())).thenReturn(existingNote);
        when(repoMock.save(existingNote)).thenReturn(existingNote);
        when(eventRepoMock.save(any(NoteEvent.class))).thenAnswer(returnsFirstArg());

        NoteDto result = sut.createUpdate(mapNoteToNoteDto(note), 41L);

        verify(authenticationServiceMock).getCurrentUser();
        verify(repoMock).findOne(note.getId());
        verify(repoMock).save(existingNote);
        ArgumentCaptor<NoteEvent> argument = ArgumentCaptor.forClass(NoteEvent.class);
        verify(eventRepoMock).save(argument.capture());

        assertEquals(new Long(42L), argument.getValue().getId().getVersion());
        assertEquals(user, argument.getValue().getUser());
        assertEquals(existingNote, argument.getValue().getNote());
        assertEquals(result.getTitle(), existingNote.getTitle());
        assertEquals(result.getContent(), existingNote.getContent());
        assertEquals("--- original\n+++ revised\n@@ -1,1 +1,1 @@\n-old content\n+content", argument.getValue().getContentDiff());
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

        sut.createUpdate(mapNoteToNoteDto(note), 0L);

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

    private NoteDto mapNoteToNoteDto(Note n) {
        NoteDto result = new NoteDto();
        result.setId(n.getId().toString());
        result.setTitle(n.getTitle());
        result.setContent(n.getContent());
        result.setVersion(n.getLastVersion());
        return result;
    }
}
