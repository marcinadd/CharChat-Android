package com.marcinadd.charchat.chat.db.model;

public enum FieldNames {
    ID("id"),
    USER_CREDENTIALS("user_credentials"),
    USERNAME("username"),
    NAME("name"),
    CHATS("chats"),
    RECEIVER("receiver"),
    CREATOR("creator"),
    CREATED_AT("createdAt"),
    RECEIVER_REFERENCE("receiver_reference"),
    CREATOR_REFERENCE("creator_reference"),
    SENDER_UID("senderUid"),
    TEXT("text"),
    IMAGE_PATH("imagePath"),
    CREATOR_HIDDEN("creator_hidden"),
    AVATAR("avatar");

    private final String fieldName;

    FieldNames(String fieldName) {
        this.fieldName = fieldName;
    }

    @Override
    public String toString() {
        return fieldName;
    }
}
