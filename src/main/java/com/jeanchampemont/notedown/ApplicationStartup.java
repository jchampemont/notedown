package com.jeanchampemont.notedown;

import com.jeanchampemont.notedown.note.NoteService;
import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    private NoteService noteService;

    private UserService userService;

    @Autowired
    public ApplicationStartup(NoteService noteService, UserService userService) {
        this.noteService = noteService;
        this.userService = userService;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent e) {
        User user = userService.create("admin@world.com", "admin");
        noteService.save(user, new Note("Test 1", "BLA BLA BLA"));
        noteService.save(user, new Note("Test 2", "BLA BLA BLA"));
        noteService.save(user, new Note("Test 3", "BLA BLA BLA"));
        noteService.save(user, new Note("Test 4", "BLA BLA BLA"));
    }
}
