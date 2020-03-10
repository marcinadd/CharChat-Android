package com.marcinadd.charchat.chat.service.listener;

import com.marcinadd.charchat.chat.db.model.ChatMessage;

public interface OnNewChatMessageArrivedListener {
    void onNewChatMessageArrived(ChatMessage chatMessage);
}
