package com.jeanchampemont.notedown.note.persistence;

import com.jeanchampemont.notedown.user.persistence.User;
import org.hibernate.annotations.Type;

import javax.persistence.*;
import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "note")
public class Note {
    @Id
    @Type(type = "uuid-char")
    private UUID id;

    @Column(name = "title", length = 64, nullable = false)
    private String title;

    @Column(name = "content", nullable = false)
    @Lob
    private String content;

    @Column(name = "last_modification", nullable = false)
    @Temporal(TemporalType.TIMESTAMP)
    private Date lastModification;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Note() {
        id = UUID.randomUUID();
    }

    public Note(String title, String content) {
        id = UUID.randomUUID();
        this.title = title;
        this.content = content;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Date getLastModification() {
        return lastModification;
    }

    public void setLastModification(Date lastModification) {
        this.lastModification = lastModification;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
