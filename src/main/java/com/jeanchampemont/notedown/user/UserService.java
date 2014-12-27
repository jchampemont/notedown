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

import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.user.persistence.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;

@Service
public class UserService {

    private UserRepository repo;

    private PasswordEncoder encoder;

    @Autowired
    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    @Transactional
    public User create(String email, String password) {
        User result = new User();
        result.setEmail(email);
        result.setPassword(encoder.encode(password));
        result.setNotes(new HashSet<>());
        result = repo.save(result);
        return result;
    }

    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return repo.findByEmail(email);
    }

    @Transactional
    public User setLocale(String locale) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = repo.findByEmail(email);
        return setLocale(user, locale);
    }

    @Transactional
    public User setLocale(User user, String locale) {
        user.setLocale(locale);
        return repo.save(user);
    }

    @Transactional(readOnly = true)
    public User getCurrentUser() {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return repo.findByEmail(email);
    }

    @Transactional
    public boolean changeEmail(User user, String email, String password) {
        if(encoder.matches(password, user.getPassword())) {
            user.setEmail(email);
            repo.save(user);
            return true;
        } else {
            return false;
        }
    }

    @Transactional
    public boolean changePassword(User user, String oldPassword, String newPassword) {
        if(encoder.matches(oldPassword, user.getPassword())) {
            user.setPassword(encoder.encode(newPassword));
            repo.save(user);
            return true;
        } else {
            return false;
        }
    }
}
