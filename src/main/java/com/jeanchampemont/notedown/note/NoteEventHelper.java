/*
 * Copyright (C) 2015 NoteDown
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
package com.jeanchampemont.notedown.note;

import com.jeanchampemont.notedown.note.persistence.Note;
import com.jeanchampemont.notedown.note.persistence.NoteEvent;
import com.jeanchampemont.notedown.note.persistence.NoteEventId;
import com.jeanchampemont.notedown.note.persistence.NoteEventType;
import com.jeanchampemont.notedown.user.persistence.User;
import difflib.DiffUtils;
import difflib.Patch;
import difflib.PatchFailedException;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class NoteEventHelper {
    public static NoteEventBuilder builder() {
        return new NoteEventBuilder();
    }

    public static Note unPatch(List<NoteEvent> events, Note note) throws PatchFailedException {
        List<String> noteContent = Arrays.asList(note.getContent().split("\n"));
        for (NoteEvent event : events) {
            Patch<String> patch = DiffUtils.parseUnifiedDiff(Arrays.asList(event.getContentDiff().split("\n")));
            noteContent = patch.restore(noteContent);
        }
        NoteEvent lastEvent = events.get(events.size() - 1);
        note.setLastModification(lastEvent.getDate());
        note.setTitle(lastEvent.getTitle());
        note.setContent(String.join("\n", noteContent));
        return note;
    }

    public static class NoteEventBuilder {
        private UUID noteId;
        private long version;
        private User user;
        private NoteEventType type;
        private String title;
        private String original;
        private String revised;

        public NoteEventBuilder noteId(UUID noteId) {
            this.noteId = noteId;
            return this;
        }

        public NoteEventBuilder version(long version) {
            this.version = version;
            return this;
        }

        public NoteEventBuilder user(User user) {
            this.user = user;
            return this;
        }

        public NoteEventBuilder save() {
            type = NoteEventType.SAVE;
            return this;
        }

        public NoteEventBuilder compress() {
            type = NoteEventType.HISTORY_COMPRESS;
            return this;
        }

        public NoteEventBuilder title(String title) {
            this.title = title;
            return this;
        }

        public NoteEventBuilder diff(String original, String revised) {
            this.original = original;
            this.revised = revised;
            return this;
        }

        public NoteEvent build() {
            NoteEvent result = new NoteEvent();
            result.setId(new NoteEventId(noteId, version));
            result.setUser(user);
            result.setDate(new Date());
            result.setType(type);
            result.setTitle(title);
            result.setContentDiff(generateDiff(original, revised));

            return result;
        }

        private String generateDiff(String original, String revised) {
            List<String> originalLinesList = Arrays.asList(original.split("\n"));
            List<String> revisedLinesList = Arrays.asList(revised.split("\n"));

            Patch<String> patch = DiffUtils.diff(originalLinesList, revisedLinesList);

            List<String> diff = DiffUtils
                    .generateUnifiedDiff("original", "revised", originalLinesList, patch, 0);

            return String.join("\n", diff);
        }
    }
}
