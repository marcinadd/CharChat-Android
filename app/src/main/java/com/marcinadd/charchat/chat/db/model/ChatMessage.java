package com.marcinadd.charchat.chat.db.model;

import com.marcinadd.charchat.chat.model.Message;

import java.util.Date;

public class ChatMessage {
    private String text;
    private Date createdAt;
    private String senderUid;

    public ChatMessage(Message message) {
        this.text = message.getText();
        this.createdAt = message.getCreatedAt();
        this.senderUid = message.getUser().getId();
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
}
