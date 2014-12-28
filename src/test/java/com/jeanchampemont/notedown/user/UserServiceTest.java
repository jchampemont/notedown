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
import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.user.persistence.repository.UserRepository;
import com.jeanchampemont.notedown.utils.exception.OperationNotAllowedException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.util.Optional;

import static org.junit.Assert.*;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = NoteDownApplication.class)
public class UserServiceTest {
    private UserService sut;

    private AuthenticationService authenticationServiceMock;

    private UserRepository repoMock;

    private PasswordEncoder encoderMock;

    @Before
    public void init() {
        authenticationServiceMock = mock(AuthenticationService.class);
        repoMock = mock(UserRepository.class);
        encoderMock = mock(PasswordEncoder.class);
        sut = new UserService(authenticationServiceMock, repoMock, encoderMock);
    }

    @Test
    public void testCreate() {
        String email = "toto@tata.fr";
        String password = "mySuperSecurePassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        when(repoMock.save(user)).thenReturn(user);
        when(encoderMock.encode(password)).thenReturn(password);

        User result = sut.create(user);

        verify(encoderMock).encode(password);
        verify(repoMock).save(user);

        assertEquals(email, result.getEmail());
    }

    @Test
    public void testFindByEmailOK() {
        String email = "toto@tata.fr";

        when(repoMock.findByEmail(email)).thenReturn(new User());

        Optional<User> result = sut.getUserByEmail(email);

        verify(repoMock).findByEmail(email);

        assertTrue(result.isPresent());
    }

    @Test
    public void testFindByEmailKO() {
        String email = "toto@tata.fr";

        when(repoMock.findByEmail(email)).thenReturn(null);

        Optional<User> result = sut.getUserByEmail(email);

        verify(repoMock).findByEmail(email);

        assertFalse(result.isPresent());
    }

    @Test
    public void testUpdate() {
        String locale = "fr";
        String email = "titi@toto.fr";

        User user = new User();
        user.setId(12);
        user.setEmail(email);
        user.setLocale(locale);

        when(repoMock.findByEmail(email)).thenReturn(user);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.save(user)).thenReturn(user);

        User result = sut.update(user);

        verify(repoMock).findByEmail(email);
        verify(repoMock).save(user);
        verify(authenticationServiceMock).getCurrentUser();
        assertEquals(locale, user.getLocale());
    }

    @Test(expected = OperationNotAllowedException.class)
    public void testUpdateNotAllowed() {
        String locale = "fr";
        String email = "titi@toto.fr";

        User user = new User();
        user.setId(12);
        user.setEmail(email);
        user.setLocale(locale);

        User notAuthorizedUser = new User();
        notAuthorizedUser.setId(42);

        when(repoMock.findByEmail(email)).thenReturn(user);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(notAuthorizedUser);

        User result = sut.update(user);

        verify(repoMock).findByEmail(email);
        verify(authenticationServiceMock).getCurrentUser();
    }

    @Test
    public void testChangeEmailOK() {
        String email = "toto@tata.fr";
        String newEmail = "tata@toto.fr";
        String password = "password";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        when(repoMock.findByEmail(email)).thenReturn(user);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(repoMock.save(user)).thenReturn(user);
        when(encoderMock.matches(password, password)).thenReturn(true);

        boolean success = sut.changeEmail(user, newEmail, password);

        verify(repoMock).findByEmail(email);
        verify(repoMock).save(user);
        verify(authenticationServiceMock).getCurrentUser();

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

        when(repoMock.findByEmail(email)).thenReturn(user);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(encoderMock.matches(anyString(), eq(password))).thenReturn(false);

        boolean success = sut.changeEmail(user, newEmail, "wrongPassword");

        verify(repoMock).findByEmail(email);
        verify(encoderMock).matches(anyString(), eq(password));

        assertFalse(success);
    }

    @Test
    public void testChangeEmailNotAllowed() {
        String email = "toto@tata.fr";
        String newEmail = "tata@toto.fr";
        String password = "password";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        User notAuthorizedUser = new User();
        notAuthorizedUser.setId(42);

        when(repoMock.findByEmail(email)).thenReturn(user);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(notAuthorizedUser);

        boolean success = sut.changeEmail(user, newEmail, password);

        verify(repoMock).findByEmail(email);

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

        when(repoMock.findByEmail(email)).thenReturn(user);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(encoderMock.matches(password, password)).thenReturn(true);
        when(encoderMock.encode(newPassword)).thenReturn(newPassword);
        when(repoMock.save(user)).thenReturn(user);

        boolean success = sut.changePassword(user, password, newPassword);

        verify(repoMock).findByEmail(email);
        verify(authenticationServiceMock).getCurrentUser();
        verify(encoderMock).matches(password, password);
        verify(encoderMock).encode(newPassword);
        verify(repoMock).save(user);

        assertTrue(success);
    }

    @Test
    public void testChangePasswordKO() {
        String email = "toto@tata.fr";
        String password = "password";
        String newPassword = "superSafePassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        when(repoMock.findByEmail(email)).thenReturn(user);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(user);
        when(encoderMock.matches("wrongPassword", password)).thenReturn(false);

        boolean success = sut.changePassword(user, "wrongPassword", newPassword);

        verify(repoMock).findByEmail(email);
        verify(encoderMock).matches(anyString(), eq(password));

        assertFalse(success);
    }

    @Test
    public void testChangePasswordNotAllowed() {
        String email = "toto@tata.fr";
        String password = "password";
        String newPassword = "superSafePassword";

        User user = new User();
        user.setEmail(email);
        user.setPassword(password);

        User notAuthorizedUser = new User();
        notAuthorizedUser.setId(42);

        when(repoMock.findByEmail(email)).thenReturn(user);
        when(authenticationServiceMock.getCurrentUser()).thenReturn(notAuthorizedUser);

        boolean success = sut.changePassword(user, password, newPassword);

        verify(repoMock).findByEmail(email);

        assertFalse(success);
    }
}
