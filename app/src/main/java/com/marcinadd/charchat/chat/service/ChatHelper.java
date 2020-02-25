package com.marcinadd.charchat.chat.service;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.marcinadd.charchat.chat.db.model.Chat;
import com.marcinadd.charchat.chat.db.model.ChatBuilder;
import com.marcinadd.charchat.chat.db.model.FieldNames;
import com.marcinadd.charchat.chat.model.User;

import java.util.Date;
import java.util.Map;

public class ChatHelper {
    private static final ChatHelper ourInstance = new ChatHelper();

    private ChatHelper() {
    }

    public static ChatHelper getInstance() {
        return ourInstance;
    }

    public User createUserFromMap(Map<String, Object> map) {
        return new User((String) map.get("id"), (String) map.get("username"), null);
    }

    public Chat createChatFromQueryDocumentSnapshot(QueryDocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        User creator = createUserFromMap((Map<String, Object>) data.get(FieldNames.CREATOR.toString()));
        User receiver = createUserFromMap((Map<String, Object>) data.get(FieldNames.RECEIVER.toString()));
        boolean creatorHidden = (boolean) data.get(FieldNames.CREATOR_HIDDEN.toString());
        Timestamp createdAt = (Timestamp) data.get(FieldNames.CREATED_AT.toString());
        return new ChatBuilder()
                .setId(document.getId())
                .setCreator(creator)
                .setReceiver(receiver)
                .setCreatorHidden(creatorHidden)
                .setCreatedAt(createdAt != null ? createdAt.toDate() : new Date())
                .createChat();
    }
}
