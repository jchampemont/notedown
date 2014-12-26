package com.jeanchampemont.notedown.user;


import com.jeanchampemont.notedown.NoteDownApplication;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.user.persistence.repository.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentMatcher;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.HashSet;

import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NoteDownApplication.class)
public class UserServiceTest {
    private UserService sut;

    private UserRepository repoMock;

    private PasswordEncoder encoderMock;

    @Before
    public void init() {
        repoMock = mock(UserRepository.class);
        encoderMock = mock(PasswordEncoder.class);
        sut = new UserService(repoMock, encoderMock);
    }

    @Test
    public void testCreate() {
        String email = "toto@tata.fr";
        String password = "mySuperSecurePassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);
        user.setNotes(new HashSet<>());

        when(repoMock.save(argThat(new UserMatcher(email, password)))).thenReturn(user);
        when(encoderMock.encode(password)).thenReturn(password);

        sut.create(email, password);

        verify(encoderMock).encode(password);
        verify(repoMock).save(argThat(new UserMatcher(email, password)));
    }

    private class UserMatcher extends ArgumentMatcher<User> {

        private String email;
        private String password;

        private UserMatcher(String email, String password) {
            this.email = email;
            this.password = password;
        }

        @Override
        public boolean matches(Object u) {
            return ((User) u).getEmail().equals(email) && ((User) u).getPassword().equals(password);
        }
    }
}
