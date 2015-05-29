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
package com.jeanchampemont.notedown.note.persistence;

import com.jeanchampemont.notedown.user.persistence.User;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "note_event")
public class NoteEvent {

    @Id
    private NoteEventId id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "note_id", referencedColumnName = "id", insertable = false, updatable = false)
    private Note note;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "date", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date date;

    @Column(name = "type")
    @Enumerated(EnumType.STRING)
    private NoteEventType type;

    @Column(name = "title", length = 64, nullable = false)
    private String title;

    @Column(name = "content_diff", nullable = false)
    @Lob
    @Type(type = "org.hibernate.type.StringClobType")
    private String contentDiff;

    public NoteEventId getId() {
        return id;
    }

    public void setId(NoteEventId id) {
        this.id = id;
    }

    public Note getNote() {
        return note;
    }

    public void setNote(Note note) {
        this.note = note;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public NoteEventType getType() {
        return type;
    }

    public void setType(NoteEventType type) {
        this.type = type;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContentDiff() {
        return contentDiff;
    }

    public void setContentDiff(String contentDiff) {
        this.contentDiff = contentDiff;
    }
}
