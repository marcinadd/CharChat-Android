package com.marcinadd.charchat.chat.db.model;

import com.marcinadd.charchat.chat.model.User;

import java.util.Date;

public class Chat {
    private String id;
    private User creator;
    private User receiver;
    private Date createdAt;
    private boolean creatorHidden;

    public Chat(String id, User creator, User receiver, Date createdAt, boolean creatorHidden) {
        this.id = id;
        this.creator = creator;
        this.receiver = receiver;
        this.createdAt = createdAt;
        this.creatorHidden = creatorHidden;
    }

    public Chat() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public User getCreator() {
        return creator;
    }

    public void setCreator(User creator) {
        this.creator = creator;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isCreatorHidden() {
        return creatorHidden;
    }

    public void setCreatorHidden(boolean creatorHidden) {
        this.creatorHidden = creatorHidden;
    }
}
