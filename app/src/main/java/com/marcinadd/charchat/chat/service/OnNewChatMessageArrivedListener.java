package com.marcinadd.charchat.chat.service;

import com.marcinadd.charchat.chat.db.model.ChatMessage;

public interface OnNewChatMessageArrivedListener {
    void onNewChatMessageArrived(ChatMessage chatMessage);
}
