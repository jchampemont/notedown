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
package com.jeanchampemont.notedown.user;

import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.user.persistence.repository.UserRepository;
import com.jeanchampemont.notedown.utils.exception.OperationNotAllowedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * This service manages users of the application.
 *
 * @author Jean Champémont
 */
@Service
public class UserService {

    private AuthenticationService authenticationService;

    private UserRepository repo;

    private PasswordEncoder encoder;

    @Autowired
    public UserService(AuthenticationService authenticationService, UserRepository repo, PasswordEncoder encoder) {
        this.authenticationService = authenticationService;
        this.repo = repo;
        this.encoder = encoder;
    }

    /**
     *
     * @return whether or not the application has at least one registered user.
     */
    @Transactional(readOnly = true)
    public boolean hasRegisteredUser() {
        return repo.findAll().iterator().hasNext();
    }

    /**
     * Create the user.
     * The password is encoded.
     * @param user
     * @return persisted user with encoded password
     */
    @Transactional
    public User create(User user) {
        user.setPassword(encoder.encode(user.getPassword()));
        user.setEmail(user.getEmail().toLowerCase());
        user.setDisplayName(user.getEmail().toLowerCase());
        user = repo.save(user);
        return user;
    }

    /**
     * Find a user for this email if it exists
     * @param email
     * @return
     */
    @Transactional(readOnly = true)
    @Cacheable("user")
    public Optional<User> getUserByEmail(String email) {
        return Optional.ofNullable(repo.findByEmailIgnoreCase(email));
    }

    /**
     * Update user
     * This method does not update email or password.
     * Use changeEmail or changePassword instead.
     * @param user
     * @return updated user
     */
    @Transactional
    @CacheEvict(value = "user", key = "#user.email")
    public User update(User user) {
        User originalUser = repo.findByEmailIgnoreCase(user.getEmail());
        User currentUser = authenticationService.getCurrentUser();
        if (hasWriteAccess(currentUser, originalUser)) {
            originalUser.setLocale(user.getLocale());
            originalUser.setDisplayName(user.getDisplayName());
            return repo.save(originalUser);
        }
        throw new OperationNotAllowedException();
    }

    /**
     * Change user's email
     * @param user
     * @param email
     * @param password
     * @return whether or not the change wass sucessfull
     */
    @Transactional
    @CacheEvict(value = "user", key = "#user.email")
    public boolean changeEmail(User user, String email, String password) {
        User originalUser = repo.findByEmailIgnoreCase(user.getEmail());
        User currentUser = authenticationService.getCurrentUser();
        if (hasWriteAccess(currentUser, originalUser) && encoder.matches(password, currentUser.getPassword())) {
            originalUser.setEmail(email);
            repo.save(originalUser);
            return true;
        } else {
            return false;
        }
    }

    /**
     * Change user's password
     * @param user
     * @param oldPassword
     * @param newPassword
     * @return whether or not the change was sucessfull
     */
    @Transactional
    @CacheEvict(value = "user", key = "#user.email")
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        User originalUser = repo.findByEmailIgnoreCase(user.getEmail());
        User currentUser = authenticationService.getCurrentUser();
        if (hasWriteAccess(currentUser, originalUser) && encoder.matches(oldPassword, currentUser.getPassword())) {
            originalUser.setPassword(encoder.encode(newPassword));
            repo.save(originalUser);
            return true;
        } else {
            return false;
        }
    }

    /**
     * @param currentUser
     * @param targetUser
     * @return whether or not the currentUser has write access to the targetUser
     */
    private boolean hasWriteAccess(User currentUser, User targetUser) {
        return currentUser.getId() == targetUser.getId();
    }
}
