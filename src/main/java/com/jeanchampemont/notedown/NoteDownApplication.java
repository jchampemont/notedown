package com.jeanchampemont.notedown;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

@Configuration
@ComponentScan
@EnableAutoConfiguration
@EnableWebSecurity
public class NoteDownApplication {

    public static void main(String[] args) {
        SpringApplication.run(NoteDownApplication.class, args);
    }

}
