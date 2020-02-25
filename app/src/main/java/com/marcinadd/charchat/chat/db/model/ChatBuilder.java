package com.marcinadd.charchat.chat.db.model;

import com.marcinadd.charchat.chat.model.User;

import java.util.Date;

public class ChatBuilder {
    private String id;
    private User creator;
    private User receiver;
    private Date createdAt;
    private boolean creatorHidden;

    public ChatBuilder setId(String id) {
        this.id = id;
        return this;
    }

    public ChatBuilder setCreator(User creator) {
        this.creator = creator;
        return this;
    }

    public ChatBuilder setReceiver(User receiver) {
        this.receiver = receiver;
        return this;
    }

    public ChatBuilder setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
        return this;
    }

    public ChatBuilder setCreatorHidden(boolean creatorHidden) {
        this.creatorHidden = creatorHidden;
        return this;
    }

    public Chat createChat() {
        return new Chat(id, creator, receiver, createdAt, creatorHidden);
    }
}