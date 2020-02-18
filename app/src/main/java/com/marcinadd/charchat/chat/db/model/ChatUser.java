package com.marcinadd.charchat.chat.db.model;

import com.google.firebase.firestore.DocumentReference;

public class ChatUser {

    private String uid;
    private String username;
    private DocumentReference reference;

    public ChatUser(String uid, String username) {
        this.uid = uid;
        this.username = username;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

}
