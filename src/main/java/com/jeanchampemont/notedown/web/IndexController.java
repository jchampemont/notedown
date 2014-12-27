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

import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.context.request.WebRequest;

@Controller
@RequestMapping("/")
public class IndexController {
    @RequestMapping(method = RequestMethod.GET)
    public String home(ModelMap model) {
        return "home";
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public String login(WebRequest req, ModelMap model) {
        if (req.getParameter("error") != null) {
            model.put("error", true);
        } else if (req.getParameter("logout") != null) {
            model.put("logout", true);
        }
        return "login";
    }
}
