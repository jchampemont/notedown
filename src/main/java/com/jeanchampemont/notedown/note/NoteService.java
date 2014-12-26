package com.jeanchampemont.notedown.note;

import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.note.persistence.repository.NoteRepository;
import com.jeanchampemont.notedown.user.UserService;
import com.jeanchampemont.notedown.user.persistence.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.UUID;

@Service
public class NoteService {

    private NoteRepository repo;

    private UserService userService;

    @Autowired
    public NoteService(NoteRepository repo, UserService userService) {
        this.repo = repo;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public Iterable<Note> getAll() {
        return repo.findByOrderByLastModificationDesc();
    }

    @Transactional(readOnly = true)
    public Note get(UUID id) {
        return repo.findOne(id);
    }

    @Transactional
    public Note save(Note note) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByEmail(email);
        return save(user, note);
    }

    @Transactional
    public Note save(User user, Note note) {
        note = updateLastModification(note);
        note.setUser(user);
        return repo.save(note);
    }

    @Transactional
    public void delete(UUID id) {
        repo.delete(id);
    }

    private Note updateLastModification(Note note) {
        note.setLastModification(new Date());
        return note;
    }
}
