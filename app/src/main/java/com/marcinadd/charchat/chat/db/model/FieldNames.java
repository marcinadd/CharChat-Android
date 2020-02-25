package com.marcinadd.charchat.chat.db.model;

public enum FieldNames {
    USER_CREDENTIALS("user_credentials"),
    USERNAME("username"),
    CHATS("chats"),
    RECEIVER("receiver"),
    CREATOR("creator"),
    CREATED_AT("createdAt"),
    RECEIVER_REFERENCE("receiver_reference"),
    CREATOR_REFERENCE("creator_reference"),
    CREATOR_HIDDEN("creator_hidden");

    private final String fieldName;

    FieldNames(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
