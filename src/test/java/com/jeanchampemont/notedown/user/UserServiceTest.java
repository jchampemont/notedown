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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
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

    @Test
    public void testFindByEmail() {
        String email = "toto@tata.fr";

        User user = new User();

        when(repoMock.findByEmail(email)).thenReturn(user);

        sut.findByEmail(email);

        verify(repoMock).findByEmail(email);
    }

    @Test
    public void testSetLocale() {
        String locale = "fr";

        User user = new User();

        when(repoMock.save(user)).thenReturn(user);

        User result = sut.setLocale(user, locale);

        verify(repoMock).save(user);
        assertEquals(locale, user.getLocale());
    }

    @Test
    public void testChangeEmailOK() {
        String email = "toto@tata.fr";
        String newEmail = "tata@toto.fr";
        String password = "password";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        User newUser = new User();
        newUser.setEmail(newEmail);
        newUser.setPassword(password);

        when(repoMock.save(argThat(new UserMatcher(newEmail, password)))).thenReturn(newUser);
        when(encoderMock.matches(password, password)).thenReturn(true);

        boolean success = sut.changeEmail(user, newEmail, password);

        verify(repoMock).save(argThat(new UserMatcher(newEmail, password)));
        verify(encoderMock).matches(password, password);

        assertTrue(success);
    }

    @Test
    public void testChangeEmailKO() {
        String email = "toto@tata.fr";
        String newEmail = "tata@toto.fr";
        String password = "password";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        when(encoderMock.matches(anyString(), eq(password))).thenReturn(false);

        boolean success = sut.changeEmail(user, newEmail, "wrongPassword");

        verify(encoderMock).matches(anyString(), eq(password));

        assertFalse(success);
    }

    @Test
    public void testChangePasswordOK() {
        String email = "toto@tata.fr";
        String password = "password";
        String newPassword = "superSafePassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        User newUser = new User();
        newUser.setEmail(email);
        newUser.setPassword(newPassword);

        when(repoMock.save(argThat(new UserMatcher(email, newPassword)))).thenReturn(newUser);
        when(encoderMock.matches(password, password)).thenReturn(true);
        when(encoderMock.encode(newPassword)).thenReturn(newPassword);

        boolean success = sut.changePassword(user, password, newPassword);

        verify(repoMock).save(argThat(new UserMatcher(email, newPassword)));
        verify(encoderMock).matches(password, password);
        verify(encoderMock).encode(newPassword);

        assertTrue(success);
    }

    @Test
    public void testChangePasswordKO() {
        String email = "toto@tata.fr";
        String password = "password";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        when(encoderMock.matches(anyString(), eq(password))).thenReturn(false);

        boolean success = sut.changePassword(user, password, "wrongPassword");

        verify(encoderMock).matches(anyString(), eq(password));

        assertFalse(success);
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
