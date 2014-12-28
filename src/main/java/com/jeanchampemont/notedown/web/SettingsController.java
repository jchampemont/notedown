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
package com.jeanchampemont.notedown.web;

import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.web.form.SettingsLanguageForm;
import com.jeanchampemont.notedown.web.form.SettingsUserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/app/settings")
public class SettingsController {

    private UserService userService;

    private AuthenticationService authenticationService;

    @Autowired
    public SettingsController(UserService userService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationService = authenticationService;
    }

    @Value("${notedown.available-locales}")
    private String availableLocales;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap model) {
        return "redirect:/app/settings/user";
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public String user(ModelMap model) {
        model.put("tab", "user");
        model.put("user", authenticationService.getCurrentUser());
        return "settings";
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public String updateUser(SettingsUserForm form, ModelMap model) {
        User user = authenticationService.getCurrentUser();

        boolean success = true;
        boolean hasChanged = false;
        if (!user.getEmail().equals(form.getEmail())) {
            hasChanged = true;
            Optional<User> existingUser = userService.getUserByEmail(form.getEmail());
            if (!existingUser.isPresent()) {
                success = userService.changeEmail(user, form.getEmail(), form.getOldPassword());
                if (!success) {
                    model.put("wrongPassword", true);
                } else {
                    //Changing email need new authentication
                    authenticationService.newAuthentication(user.getEmail(), form.getOldPassword());
                }
            } else {
                success = false;
                model.put("emailExists", true);
            }
        }
        if (success && !StringUtils.isEmpty(form.getNewPassword())) {
            hasChanged = true;
            success = userService.changePassword(user, form.getOldPassword(), form.getNewPassword());
            if (!success) {
                model.put("wrongPassword", true);
            }
        }
        model.put("success", success && hasChanged);
        model.put("tab", "user");
        return user(model);
    }

    @RequestMapping(value = "/lang", method = RequestMethod.GET)
    public String lang(ModelMap model) {
        model.put("tab", "lang");
        model.put("availableLanguages", availableLanguages());
        model.put("selectedLanguage", authenticationService.getCurrentUser().getLocale());
        return "settings";
    }

    @RequestMapping(value = "/lang", method = RequestMethod.POST)
    public String updateLang(SettingsLanguageForm form, ModelMap model) {
        User user = authenticationService.getCurrentUser();
        if (!user.getLocale().equals(form.getLocale())) {
            user.setLocale(form.getLocale());
            userService.update(user);
        }
        model.put("success", true);
        return lang(model);
    }

    private List<String> availableLanguages() {
        return new ArrayList<String>(Arrays.asList(availableLocales.split(",")));
    }
}
