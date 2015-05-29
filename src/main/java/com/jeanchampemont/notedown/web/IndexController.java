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
package com.jeanchampemont.notedown.web;

import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.web.form.InstallForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/")
public class IndexController {

    private UserService userService;

    @Autowired
    public IndexController(UserService userService) {
        this.userService = userService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String home(ModelMap model) {
        return "home";
    }

    @RequestMapping(value = "login", method = RequestMethod.GET)
    public String login(WebRequest req, ModelMap model) {
        if (userService.hasRegisteredUser()) {
            if (req.getParameter("error") != null) {
                model.put("error", true);
            } else if (req.getParameter("logout") != null) {
                model.put("logout", true);
            } else if (req.getParameter("install") != null) {
                model.put("install", true);
            }
            return "login";
        } else {
            model.put("form", new InstallForm());
            return "welcome";
        }
    }

    @RequestMapping(value = "welcome", method = RequestMethod.POST)
    public String install(InstallForm form, ModelMap model) {
        //Only the first account should be created from here.
        if (!userService.hasRegisteredUser()) {
            model.put("form", form);
            //Basic email validation...
            if (!form.getEmail().contains("@")) {
                model.put("wrongEmail", true);
                return "welcome";
            } else if (form.getPassword().length() < 6) {
                model.put("wrongPassword", true);
                return "welcome";
            } else if (!form.getPassword().equals(form.getPasswordConfirmation())) {
                model.put("wrongConfirmation", true);
                return "welcome";
            }
            User user = new User();
            user.setEmail(form.getEmail());
            user.setPassword(form.getPassword());
            userService.create(user);
        }
        return "redirect:/login?install";
    }
}
