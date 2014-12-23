package com.jeanchampemont.notedown;

import com.jeanchampemont.notedown.note.NoteService;
import com.jeanchampemont.notedown.note.persistence.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

@Component
public class ApplicationStartup implements ApplicationListener<ContextRefreshedEvent> {

    private NoteService noteService;

    @Autowired
    public ApplicationStartup(NoteService noteService) {
        this.noteService = noteService;
    }

    @Override
    public void onApplicationEvent(final ContextRefreshedEvent e) {
        noteService.save(new Note("Test 1", "BLA BLA BLA"));
        noteService.save(new Note("Test 2", "BLA BLA BLA"));
        noteService.save(new Note("Test 3", "BLA BLA BLA"));
        noteService.save(new Note("Test 4", "BLA BLA BLA"));
    }
}
