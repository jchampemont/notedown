package com.jeanchampemont.notedown.utils;

import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Locale;

/**
 * A LocaleResolver which use User's settings to determine Locale.
 * If user is not connected, defaults to AcceptHeaderLocaleResolver.
 */
public class UserLocaleResolver implements LocaleResolver {

    private UserService userService;

    private AcceptHeaderLocaleResolver acceptHeaderLocaleResolver;

    public UserLocaleResolver(UserService userService) {
        this.userService = userService;
        this.acceptHeaderLocaleResolver = new AcceptHeaderLocaleResolver();
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        if(SecurityContextHolder.getContext().getAuthentication() == null) {
            return acceptHeaderLocaleResolver.resolveLocale(request);
        }
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email);
        if(user == null || StringUtils.isEmpty(user.getLocale())) {
            return acceptHeaderLocaleResolver.resolveLocale(request);
        }
        return Locale.forLanguageTag(user.getLocale());
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        userService.setLocale(locale.getLanguage());
    }
}
