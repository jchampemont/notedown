package com.jeanchampemont.notedown.note;

import com.jeanchampemont.notedown.NoteDownApplication;
import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.note.persistence.repository.NoteRepository;
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

    private UserRepository userRepoMock;

    @Before
    public void init() {
        repoMock = mock(NoteRepository.class);
        userRepoMock = mock(UserRepository.class);
        sut = new NoteService(repoMock, userRepoMock);
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
