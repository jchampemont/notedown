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
package com.jeanchampemont.notedown.config;

import com.jeanchampemont.notedown.security.AuthenticationService;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.utils.PrettyTimeInterceptor;
import com.jeanchampemont.notedown.utils.SecurityInterceptor;
import com.jeanchampemont.notedown.utils.UserLocaleResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.CharacterEncodingFilter;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

@Configuration
public class WebMvcConfiguration extends WebMvcConfigurerAdapter {

    @Autowired
    SecurityInterceptor securityInterceptor;

    @Autowired
    PrettyTimeInterceptor prettyTimeInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(securityInterceptor);
        registry.addInterceptor(prettyTimeInterceptor);
    }

    @Bean
    public LocaleResolver localeResolver(UserService userService, AuthenticationService authenticationService) {
        UserLocaleResolver ulr = new UserLocaleResolver(userService, authenticationService);
        return ulr;
    }

    @Bean
    public CharacterEncodingFilter characterEncodingFilter() {
        CharacterEncodingFilter characterEncodingFilter = new CharacterEncodingFilter();
        characterEncodingFilter.setEncoding("UTF-8");
        return characterEncodingFilter;
    }
}
