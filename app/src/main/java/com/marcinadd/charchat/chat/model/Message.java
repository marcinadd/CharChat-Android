package com.marcinadd.charchat.chat.model;

import androidx.annotation.Nullable;

import com.stfalcon.chatkit.commons.models.IMessage;
import com.stfalcon.chatkit.commons.models.MessageContentType;

import java.util.Date;

public class Message implements IMessage, MessageContentType.Image {

    private String id;
    private String text;
    private User user;
    private Date createdAt;
    private String imageUrl;

    public Message(String id, String text, User user, Date createdAt) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
    }

    public Message(String id, String text, User user, Date createdAt, String imageUrl) {
        this.id = id;
        this.text = text;
        this.user = user;
        this.createdAt = createdAt;
        this.imageUrl = imageUrl;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getText() {
        return text;
    }

    @Override
    public User getUser() {
        return user;
    }

    @Override
    public Date getCreatedAt() {
        return createdAt;
    }

    @Nullable
    @Override
    public String getImageUrl() {
        return imageUrl;
    }
}
