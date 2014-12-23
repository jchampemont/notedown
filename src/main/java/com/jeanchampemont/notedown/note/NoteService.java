package com.jeanchampemont.notedown.note;

import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.note.persistence.repository.NoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

@Service
public class NoteService {
    private NoteRepository repo;

    @Autowired
    public NoteService(NoteRepository repo) {
        this.repo = repo;
    }

    public Iterable<Note> getAll() {
        return repo.findByOrderByLastModificationDesc();
    }

    public Note get(UUID id) {
        return repo.findOne(id);
    }

    public Note save(Note note) {
        note = updateLastModification(note);
        note = repo.save(note);
        return note;
    }

    public void delete(UUID id) {
        repo.delete(id);
    }

    private Note updateLastModification(Note note) {
        note.setLastModification(new Date());
        return note;
    }
}
