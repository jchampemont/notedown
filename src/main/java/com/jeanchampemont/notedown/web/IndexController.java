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
