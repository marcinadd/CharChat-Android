package com.marcinadd.charchat.chat.db.model;

import com.marcinadd.charchat.chat.model.Message;

import java.util.Date;

public class ChatMessage {
    private String id;
    private String text;
    private Date createdAt;
    private String senderUid;
    private String recipientUid;
    private String imagePath;

    public ChatMessage(Message message, String recipientUid) {
        this.text = message.getText();
        this.createdAt = message.getCreatedAt();
        this.senderUid = message.getUser().getId();
        this.imagePath = message.getImageUrl();
        this.recipientUid = recipientUid;
    }

    public ChatMessage() {
    }

    public String getText() {
        return text;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public String getSenderUid() {
        return senderUid;
    }

    public String getRecipientUid() {
        return recipientUid;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getImagePath() {
        return imagePath;
    }
}
