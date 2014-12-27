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
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.user.persistence.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Collections;
import java.util.Date;
import java.util.UUID;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NoteDownApplication.class)
public class NoteServiceTest {
    private NoteService sut;

    private NoteRepository repoMock;

    private UserService userServiceMock;

    @Before
    public void init() {
        repoMock = mock(NoteRepository.class);
        userServiceMock = mock(UserService.class);
        sut = new NoteService(repoMock, userServiceMock);
    }

    @Test
    public void testGetAll() {
        when(repoMock.findByOrderByLastModificationDesc()).thenReturn(Collections.emptyList());
        sut.getAll();
        verify(repoMock).findByOrderByLastModificationDesc();
    }

    @Test
    public void testSave() {
        Note n = new Note("title", "content");
        User u = new User();

        Date someDate = new Date();
        n.setLastModification(someDate);

        when(repoMock.save(n)).thenReturn(n);

        n = sut.save(u, n);

        verify(repoMock).save(n);

        assertTrue(someDate.before(n.getLastModification()));
        assertTrue(u == n.getUser());
    }

    @Test
    public void testDelete() {
        Note n = new Note("title", "content");

        sut.delete(n.getId());

        verify(repoMock).delete(n.getId());
    }

}
