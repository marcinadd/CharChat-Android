package com.marcinadd.charchat.chat.service;

import com.marcinadd.charchat.chat.model.Message;

import java.util.List;

public interface OnMessagesLoadedListener {
    void onMessagesLoaded(List<Message> messages);
}
