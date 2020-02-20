package com.marcinadd.charchat.chat.service;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marcinadd.charchat.chat.db.model.ChatMessage;
import com.marcinadd.charchat.chat.db.model.ChatUser;
import com.marcinadd.charchat.chat.model.Dialog;
import com.marcinadd.charchat.chat.model.Message;
import com.marcinadd.charchat.chat.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatService {
    private static final ChatService ourInstance = new ChatService();
    private static final String USER_CREDENTIALS = "user_credentials";
    private static final String USERNAME = "username";
    private static final String CHATS = "chats";
    private static final String RECEIVER = "receiver";
    private static final String CREATOR = "creator";

    private ChatService() {
    }

    public static ChatService getInstance() {
        return ourInstance;
    }

    public void createNewChat(final String username) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        getUserByUsername(username, db, new Callback() {
            @Override
            public void onSuccess(QueryDocumentSnapshot document) {
                DocumentReference referenceCreator = db.collection(USER_CREDENTIALS).document(firebaseUser.getUid());
                DocumentReference referenceReceiver = db.collection(USER_CREDENTIALS).document(document.getId());
                Map<String, Object> chat = createChatMapArray(referenceCreator, referenceReceiver, username);


                db.collection(CHATS).document()
                        .set(chat);
//                        .addOnSuccessListener(new OnSuccessListener<Void>() {
//                            @Override
//                            public void onSuccess(Void aVoid) {
//                                Log.e("Succcess", "success");
//                            }
//                        });
            }

            @Override
            public void onFailed() {

            }
        });

    }

    public Map<String, Object> createChatMapArray(DocumentReference referenceCreator,
                                                  DocumentReference referenceReceiver,
                                                  String receiverUsername) {
        Map<String, Object> sampleChat = new HashMap<>();
        ChatUser creatorModel = new ChatUser(referenceCreator.getId().trim(), "Secret");
        ChatUser receiverModel = new ChatUser(referenceReceiver.getId().trim(), receiverUsername);
        sampleChat.put("creator_reference", referenceCreator);
        sampleChat.put("receiver_reference", referenceReceiver);
        sampleChat.put(CREATOR, creatorModel);
        sampleChat.put(RECEIVER, receiverModel);
        sampleChat.put("creator_hidden", true);
        return sampleChat;
    }

    private void getUserByUsername(String username, FirebaseFirestore db, final Callback callback) {
        Query receiverUid = db.collection(USER_CREDENTIALS).whereEqualTo(USERNAME, username);
        receiverUid.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                callback.onSuccess(document);
                            }

                        } else {
                            callback.onFailed();
                        }
                    }
                });
    }


    public void getChats(final OnDialogsLoadedListener listener) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userReference = db.collection(USER_CREDENTIALS).document(firebaseUser.getUid());


        final Task<QuerySnapshot> taskA = db.collection(CHATS)
                .whereEqualTo("receiver_reference", userReference)
                .get();

        final Task<QuerySnapshot> taskB = db.collection(CHATS)
                .whereEqualTo("creator_reference", userReference)
                .get();

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        tasks.add(taskA);
        tasks.add(taskB);

        Tasks.whenAll(tasks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        List<Dialog> dialogs = new ArrayList<>();
                        for (QueryDocumentSnapshot document : taskA.getResult()
                        ) {
                            dialogs.add(createDialogFromQueryDocumentSnapshot(document, firebaseUser.getUid()));
                        }
                        for (QueryDocumentSnapshot document : taskB.getResult()
                        ) {
                            dialogs.add(createDialogFromQueryDocumentSnapshot(document, firebaseUser.getUid()));
                        }
                        listener.onDialogsLoaded(dialogs);
                    }
                });
    }

    public Dialog createDialogFromQueryDocumentSnapshot(QueryDocumentSnapshot document, String currentUserId) {
        Map<String, Object> data = document.getData();
        HashMap<String, String> creatorMap = (HashMap<String, String>) data.get(CREATOR);
        HashMap<String, String> receiverMap = (HashMap<String, String>) data.get(RECEIVER);

        ChatUser creator = new ChatUser(creatorMap.get("uid"), creatorMap.get(USERNAME));
        ChatUser receiver = new ChatUser(receiverMap.get("uid"), receiverMap.get(USERNAME));

        User chatDialogUser;
        if (!creator.getUid().equals(currentUserId)) {
            chatDialogUser = new User(creator.getUid().trim(), creator.getUsername(), null);
        } else {
            chatDialogUser = new User(receiver.getUid().trim(), receiver.getUsername(), null);
        }

        Message message = new Message("94949", "This is sample message", chatDialogUser, new Date());
        List<User> users = Collections.singletonList(chatDialogUser);
        return new Dialog(document.getId(), null, chatDialogUser.getName(), users, message, 30);

    }

    private Message createMessageFromQueryDocumentSnapshot(QueryDocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        User user = new User((String) data.get("senderUid"), null, null);

        Timestamp timestamp = (Timestamp) data.get("createdAt");
        String text = (String) data.get("text");
        return new Message(document.getId(), text, user, timestamp.toDate());

    }

    public void sendMessage(Message message, String chatId) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(getMessagesCollectionPath(chatId))
                .document().set(new ChatMessage(message));
    }

    public void getMessagesForSpecificChat(String chatId, final OnMessagesLoadedListener listener) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection(getMessagesCollectionPath(chatId))
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    List<Message> messages = new ArrayList<>();

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            messages.add(createMessageFromQueryDocumentSnapshot(document));
                        }
                        listener.onMessagesLoaded(messages);
                    }
                });
    }

    private String getMessagesCollectionPath(String chatId) {
        return CHATS + "/" + (chatId.trim()) + "/messages";
    }


    interface Callback {
        void onSuccess(QueryDocumentSnapshot document);
        void onFailed();
    }

}
