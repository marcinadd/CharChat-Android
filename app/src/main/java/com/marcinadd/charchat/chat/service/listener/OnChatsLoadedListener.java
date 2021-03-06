package com.marcinadd.charchat.chat.service.listener;

import com.marcinadd.charchat.chat.db.model.Chat;

import java.util.List;

public interface OnChatsLoadedListener {
    void onChatsLoaded(List<Chat> chats, String otherUserUId, String otherUserUsername);
}
