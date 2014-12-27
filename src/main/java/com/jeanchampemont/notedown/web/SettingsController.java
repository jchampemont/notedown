package com.jeanchampemont.notedown.web;

import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import com.jeanchampemont.notedown.web.form.SettingsLanguageForm;
import com.jeanchampemont.notedown.web.form.SettingsUserForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/app/settings")
public class SettingsController {

    private UserService userService;

    private AuthenticationManager authenticationManager;

    @Autowired
    public SettingsController(UserService userService, AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @Value("${notedown.available-locales}")
    private String availableLocales;

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap model) {
        return "redirect:/app/settings/user";
    }

    @RequestMapping(value="/user", method = RequestMethod.GET)
    public String user(ModelMap model) {
        model.put("tab", "user");
        model.put("user", userService.getCurrentUser());
        return "settings";
    }

    @RequestMapping(value="/user", method = RequestMethod.POST)
    public String updateUser(SettingsUserForm form, ModelMap model) {
        User user = userService.getCurrentUser();

        boolean success = true;
        boolean hasChanged = false;
        if(!user.getEmail().equals(form.getEmail())) {
            hasChanged = true;
            User existingUser = userService.findByEmail(form.getEmail());
            if(existingUser == null) {
                success = userService.changeEmail(user, form.getEmail(), form.getOldPassword());
                if(!success) {
                    model.put("wrongPassword", true);
                } else {
                    //Changing email need new authentication
                    Authentication request = new UsernamePasswordAuthenticationToken(user.getEmail(), form.getOldPassword());
                    Authentication result = authenticationManager.authenticate(request);
                    SecurityContextHolder.getContext().setAuthentication(result);
                }
            } else {
                success = false;
                model.put("emailExists", true);
            }
        }
        if(success && !StringUtils.isEmpty(form.getNewPassword())) {
            hasChanged = true;
            success = userService.changePassword(user, form.getOldPassword(), form.getNewPassword());
            if(!success) {
                model.put("wrongPassword", true);
            }
        }
        model.put("success", success && hasChanged);
        model.put("tab", "user");
        return user(model);
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
            userService.setLocale(user, form.getLocale());
        }
        model.put("success", true);
        return lang(model);
    }

    private List<String> availableLanguages() {
        return new ArrayList<String>(Arrays.asList(availableLocales.split(",")));
    }
}
