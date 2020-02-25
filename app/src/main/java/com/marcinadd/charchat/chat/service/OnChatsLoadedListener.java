package com.marcinadd.charchat.chat.service;

import com.marcinadd.charchat.chat.db.model.Chat;

import java.util.List;

public interface OnChatsLoadedListener {
    void onChatsLoaded(List<Chat> chats, String otherUserUId);
}
