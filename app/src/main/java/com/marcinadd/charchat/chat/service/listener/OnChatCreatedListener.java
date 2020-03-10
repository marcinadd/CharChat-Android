package com.marcinadd.charchat.chat.service.listener;

public interface OnChatCreatedListener {
    void onChatCreated(String userUid, String username, String chatId);
}
