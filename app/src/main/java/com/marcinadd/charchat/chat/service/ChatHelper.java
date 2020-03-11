package com.marcinadd.charchat.chat.service;

import com.google.firebase.Timestamp;
import com.google.firebase.firestore.DocumentReference;
import com.marcinadd.charchat.chat.db.model.Chat;
import com.marcinadd.charchat.chat.db.model.ChatBuilder;
import com.marcinadd.charchat.chat.db.model.ChatMessage;
import com.marcinadd.charchat.chat.db.model.FieldNames;
import com.marcinadd.charchat.chat.model.Dialog;
import com.marcinadd.charchat.chat.model.Message;
import com.marcinadd.charchat.chat.model.User;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.marcinadd.charchat.chat.db.model.FieldNames.CREATED_AT;
import static com.marcinadd.charchat.chat.db.model.FieldNames.CREATOR;
import static com.marcinadd.charchat.chat.db.model.FieldNames.CREATOR_HIDDEN;
import static com.marcinadd.charchat.chat.db.model.FieldNames.CREATOR_REFERENCE;
import static com.marcinadd.charchat.chat.db.model.FieldNames.ID;
import static com.marcinadd.charchat.chat.db.model.FieldNames.IMAGE_PATH;
import static com.marcinadd.charchat.chat.db.model.FieldNames.NAME;
import static com.marcinadd.charchat.chat.db.model.FieldNames.RECEIVER;
import static com.marcinadd.charchat.chat.db.model.FieldNames.RECEIVER_REFERENCE;
import static com.marcinadd.charchat.chat.db.model.FieldNames.SENDER_UID;
import static com.marcinadd.charchat.chat.db.model.FieldNames.TEXT;
import static com.marcinadd.charchat.chat.db.model.FieldNames.USERNAME;

public class ChatHelper {
    private static final ChatHelper ourInstance = new ChatHelper();

    private ChatHelper() {
    }

    public static ChatHelper getInstance() {
        return ourInstance;
    }

    public User createUserFromMap(Map<String, Object> data) {
        return new User((String) data.get(ID.toString()), (String) data.get(USERNAME.toString()), null);
    }

    public Chat createChatFromMap(Map<String, Object> data, String id) {
        User creator = createUserFromMap((Map<String, Object>) data.get(FieldNames.CREATOR.toString()));
        User receiver = createUserFromMap((Map<String, Object>) data.get(FieldNames.RECEIVER.toString()));
        boolean creatorHidden = (boolean) data.get(CREATOR_HIDDEN.toString());
        Timestamp createdAt = (Timestamp) data.get(CREATED_AT.toString());
        return new ChatBuilder()
                .setId(id)
                .setCreator(creator)
                .setReceiver(receiver)
                .setCreatorHidden(creatorHidden)
                .setCreatedAt(createdAt != null ? createdAt.toDate() : new Date())
                .createChat();
    }

    public Message createMessageFromChatMessage(ChatMessage message) {
        User user = new User(message.getSenderUid(), null, null);
        return new Message(message.getId(), message.getText(), user, message.getCreatedAt(), message.getImagePath());
    }

    public Message createMessageFromMap(Map<String, Object> data, String id) {
        User user = new User((String) data.get(SENDER_UID.toString()), null, null);
        Timestamp timestamp = (Timestamp) data.get(CREATED_AT.toString());
        String text = (String) data.get(TEXT.toString());
        String imagePath = (String) data.get(IMAGE_PATH.toString());
        return new Message(id, text, user, timestamp.toDate(), imagePath);
    }

    public Dialog createDialog(Map<String, Object> data, String chatId, String currentUserId, Message message) {
        HashMap<String, String> creatorMap = (HashMap<String, String>) data.get(CREATOR.toString());
        HashMap<String, String> receiverMap = (HashMap<String, String>) data.get(RECEIVER.toString());

        User creator = new User(creatorMap.get(ID.toString()), creatorMap.get(NAME.toString()), null);
        User receiver = new User(receiverMap.get(ID.toString()), receiverMap.get(NAME.toString()), null);

        User chatDialogUser;
        if (!creator.getId().equals(currentUserId)) {
            chatDialogUser = new User(creator.getId().trim(), creator.getName(), null);
        } else {
            chatDialogUser = new User(receiver.getId().trim(), receiver.getName(), null);
        }
        List<User> users = Collections.singletonList(chatDialogUser);
        return new Dialog(chatId, null, chatDialogUser.getName(), users, message, 0);
    }

    Map<String, Object> createNewChatMap(DocumentReference referenceCreator,
                                         DocumentReference referenceReceiver,
                                         String receiverUsername) {
        Map<String, Object> sampleChat = new HashMap<>();
        User creatorModel = new User(referenceCreator.getId().trim(), "Secret", null);
        User receiverModel = new User(referenceReceiver.getId().trim(), receiverUsername, null);
        sampleChat.put(CREATOR_REFERENCE.toString(), referenceCreator);
        sampleChat.put(RECEIVER_REFERENCE.toString(), referenceReceiver);
        sampleChat.put(CREATOR.toString(), creatorModel);
        sampleChat.put(RECEIVER.toString(), receiverModel);
        sampleChat.put(CREATOR_HIDDEN.toString(), true);
        return sampleChat;
    }

}
