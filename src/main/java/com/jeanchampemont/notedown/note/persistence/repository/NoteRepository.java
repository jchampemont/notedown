package com.jeanchampemont.notedown.note.persistence.repository;

import com.jeanchampemont.notedown.note.persistence.Note;
import org.springframework.data.repository.CrudRepository;

import java.util.List;
import java.util.UUID;

public interface NoteRepository extends CrudRepository<Note, UUID> {
    List<Note> findByOrderByLastModificationDesc();
}
