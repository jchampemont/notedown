package com.jeanchampemont.notedown.web;

import com.jeanchampemont.notedown.note.NoteService;
import com.jeanchampemont.notedown.note.persistence.Note;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.UUID;

@Controller
@RequestMapping("/app")
public class NoteController {

    private NoteService noteService;

    @Autowired
    public NoteController(NoteService noteService) {
        this.noteService = noteService;
    }

    @RequestMapping(method = RequestMethod.GET)
    public String index(ModelMap model) {
        model.put("notes", noteService.getAll());
        return "list";
    }

    @RequestMapping(value = "/note/new", method = RequestMethod.GET)
    public String editNew(ModelMap model) {
        model.put("note", new Note());
        return "edit";
    }

    @RequestMapping(value = "/note/{id}", method = RequestMethod.GET)
    public String edit(@PathVariable("id") UUID id, ModelMap model) {
        model.put("note", noteService.get(id));
        return "edit";
    }

    @RequestMapping(value = "/note", method = RequestMethod.POST)
    public String save(@ModelAttribute Note note, ModelMap model) {
        noteService.save(note);
        return "redirect:/app";
    }

    @RequestMapping(value = "/note/delete/{id}", method = RequestMethod.GET)
    public String delete(@PathVariable("id") UUID id, ModelMap model) {
        noteService.delete(id);
        return "redirect:/app";
    }
}
