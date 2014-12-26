package com.jeanchampemont.notedown.web;

import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.web.form.SettingsLanguageForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/app/settings")
public class SettingsController {

    private UserService userService;

    @Autowired
    public SettingsController(UserService userService) {
        this.userService = userService;
    }

    @Value("${notedown.available-locales}")
    private String availableLocales;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap model) {
        return "redirect:/app/settings/lang";
    }

    @RequestMapping(value="/lang", method = RequestMethod.GET)
    public String lang(ModelMap model) {
        model.put("tab", "lang");
        model.put("availableLanguages", availableLanguages());
        model.put("selectedLanguage", userService.getCurrentUser().getLocale());
        return "settings";
    }

    @RequestMapping(value="/lang", method = RequestMethod.POST)
    public String updateLang(SettingsLanguageForm form, ModelMap model) {
        User user = userService.getCurrentUser();
        if(! user.getLocale().equals(form.getLocale())) {
            userService.setLocale(form.getLocale());
        }
        model.put("success", true);
        return lang(model);
    }

    private List<String> availableLanguages() {
        return new ArrayList<String>(Arrays.asList(availableLocales.split(",")));
    }
}
