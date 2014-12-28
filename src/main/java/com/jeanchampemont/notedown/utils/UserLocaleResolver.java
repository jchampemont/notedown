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
package com.jeanchampemont.notedown.utils;

import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;
import java.util.Optional;

/**
 * A LocaleResolver which use User's settings to determine Locale.
 * If user is not connected, defaults to AcceptHeaderLocaleResolver.
 */
public class UserLocaleResolver implements LocaleResolver {

    private UserService userService;

    private AuthenticationService authenticationService;

    private AcceptHeaderLocaleResolver acceptHeaderLocaleResolver;

    public UserLocaleResolver(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
        this.acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        if(SecurityContextHolder.getContext().getAuthentication() == null) {
            return acceptHeaderLocaleResolver.resolveLocale(request);
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        Optional<User> user = userService.getUserByEmail(email);
        if( ! user.isPresent() || StringUtils.isEmpty(user.get().getLocale())) {
            return acceptHeaderLocaleResolver.resolveLocale(request);
        }
        return user.map(u -> Locale.forLanguageTag(u.getLocale())).get();
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        User user = authenticationService.getCurrentUser();
        user.setLocale(locale.getLanguage());
        userService.updateUser(user);
    }
}
