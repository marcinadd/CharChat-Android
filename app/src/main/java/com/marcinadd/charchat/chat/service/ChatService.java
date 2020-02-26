package com.marcinadd.charchat.chat.service;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
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
import com.marcinadd.charchat.chat.db.model.Chat;
import com.marcinadd.charchat.chat.db.model.ChatMessage;
import com.marcinadd.charchat.chat.model.Dialog;
import com.marcinadd.charchat.chat.model.Message;
import com.marcinadd.charchat.chat.model.User;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatService {
    private static final ChatService ourInstance = new ChatService();

    private static final String USER_CREDENTIALS = "user_credentials";
    private static final String USERNAME = "username";
    private static final String NAME = "name";
    private static final String CHATS = "chats";
    private static final String RECEIVER = "receiver";
    private static final String CREATOR = "creator";
    private static final String CREATED_AT = "createdAt";
    private static final String RECEIVER_REFERENCE = "receiver_reference";
    private static final String CREATOR_REFERENCE = "creator_reference";
    private static final String CREATOR_HIDDEN = "creator_hidden";

    private ChatService() {
    }

    public static ChatService getInstance() {
        return ourInstance;
    }

    @Deprecated
    public void createNewChatViaUsername(final String username) {
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

    public void createNewChat(final String userUid, final String username, final OnChatCreatedListener listener) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference referenceCreator = db.collection(USER_CREDENTIALS).document(firebaseUser.getUid());
        DocumentReference referenceReceiver = db.collection(USER_CREDENTIALS).document(userUid.trim());
        Map<String, Object> chat = createChatMapArray(referenceCreator, referenceReceiver, username);

        db.collection(CHATS)
                .add(chat)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        listener.onChatCreated(userUid, username, documentReference.getId());
                    }
                });
    }

    //TODO Implement support for multiple chat instances
    public void getChatsForOtherAndCurrentUser(final String otherUserUid, final String otherUserUsername, final OnChatsLoadedListener listener) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference currentUser = db.collection(USER_CREDENTIALS).document(firebaseUser.getUid());
        DocumentReference otherUser = db.collection(USER_CREDENTIALS).document(otherUserUid.trim());

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        final Task<QuerySnapshot> taskA = db.collection(CHATS)
                .whereEqualTo(CREATOR_REFERENCE, otherUser)
                .whereEqualTo(RECEIVER_REFERENCE, currentUser)
                .whereEqualTo(CREATOR_HIDDEN, false)
                .get();

        final Task<QuerySnapshot> taskB = db.collection(CHATS)
                .whereEqualTo(CREATOR_REFERENCE, currentUser)
                .whereEqualTo(RECEIVER_REFERENCE, otherUser)
                .get();

        tasks.add(taskA);
        tasks.add(taskB);

        Tasks.whenAll(tasks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        List<Chat> chats = new ArrayList<>();
                        //TODO Fix this code repeat
                        for (QueryDocumentSnapshot document : taskA.getResult()) {
                            chats.add(
                                    ChatHelper.getInstance().createChatFromQueryDocumentSnapshot(document)
                            );

                        }

                        for (QueryDocumentSnapshot document : taskB.getResult()) {
                            chats.add(
                                    ChatHelper.getInstance().createChatFromQueryDocumentSnapshot(document)
                            );
                        }
                        listener.onChatsLoaded(chats, otherUserUid, otherUserUsername);
                    }
                });
    }

    private Map<String, Object> createChatMapArray(DocumentReference referenceCreator,
                                                   DocumentReference referenceReceiver,
                                                   String receiverUsername) {
        Map<String, Object> sampleChat = new HashMap<>();
        User creatorModel = new User(referenceCreator.getId().trim(), "Secret", null);
        User receiverModel = new User(referenceReceiver.getId().trim(), receiverUsername, null);
        sampleChat.put(CREATOR_REFERENCE, referenceCreator);
        sampleChat.put(RECEIVER_REFERENCE, referenceReceiver);
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
                .whereEqualTo(RECEIVER_REFERENCE, userReference)
                .get();

        final Task<QuerySnapshot> taskB = db.collection(CHATS)
                .whereEqualTo(CREATOR_REFERENCE, userReference)
                .get();

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        tasks.add(taskA);
        tasks.add(taskB);

        Tasks.whenAll(tasks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        final List<Dialog> dialogList = new ArrayList<>();
                        String currentUserId = firebaseUser.getUid();
                        createDialogs(taskA.getResult(), currentUserId, db, onDialogsCreatedListener(dialogList, listener));
                        createDialogs(taskB.getResult(), currentUserId, db, onDialogsCreatedListener(dialogList, listener));
                    }
                });
    }

    private OnDialogsCreatedListener onDialogsCreatedListener(
            final List<Dialog> dialogList,
            final OnDialogsLoadedListener listener
    ) {
        return new OnDialogsCreatedListener() {
            @Override
            public void onDialogsCreated(List<Dialog> dialogs) {
                dialogList.addAll(dialogs);
                listener.onDialogsLoaded(dialogs);
            }
        };
    }

    //     FIXME Fix Loading sorting via date
    private void createDialogs(final QuerySnapshot querySnapshot, final String currentUserId, FirebaseFirestore db, final OnDialogsCreatedListener listener) {
        final List<Dialog> dialogs = new ArrayList<>();
        final List<Task<QuerySnapshot>> tasks = new ArrayList<>();
        for (final QueryDocumentSnapshot dialogDocument : querySnapshot
        ) {
            Task<QuerySnapshot> task = db.collection(getMessagesCollectionPath(dialogDocument.getId()))
                    .orderBy(CREATED_AT, Query.Direction.DESCENDING)
                    .limit(1)
                    .get()
                    .addOnCompleteListener(onLastMessageLoadedListener(dialogs, dialogDocument, currentUserId));
            tasks.add(task);
        }

        Tasks.whenAll(tasks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onDialogsCreated(dialogs);
                    }
                });
    }

    private OnCompleteListener<QuerySnapshot> onLastMessageLoadedListener(final List<Dialog> dialogs, final QueryDocumentSnapshot dialogDocument, final String currentUserId) {
        return new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.getResult() != null && task.getResult().size() != 0) {
                    for (QueryDocumentSnapshot messageDocument : task.getResult()
                    ) {
                        Message message = createMessageFromQueryDocumentSnapshot(messageDocument);
                        Dialog dialog = createDialogFromQueryDocumentSnapshot(dialogDocument, currentUserId, message);
                        dialogs.add(dialog);
                    }
                } else {
                    Dialog dialog = createDialogFromQueryDocumentSnapshot(dialogDocument, currentUserId, null);
                    dialogs.add(dialog);
                }

            }
        };
    }

    private Dialog createDialogFromQueryDocumentSnapshot(QueryDocumentSnapshot document, String currentUserId, Message message) {
        Map<String, Object> data = document.getData();
        HashMap<String, String> creatorMap = (HashMap<String, String>) data.get(CREATOR);
        HashMap<String, String> receiverMap = (HashMap<String, String>) data.get(RECEIVER);

        User creator = new User(creatorMap.get("id"), creatorMap.get(NAME), null);
        User receiver = new User(receiverMap.get("id"), receiverMap.get(NAME), null);

        User chatDialogUser;
        if (!creator.getId().equals(currentUserId)) {
            chatDialogUser = new User(creator.getId().trim(), creator.getName(), null);
        } else {
            chatDialogUser = new User(receiver.getId().trim(), receiver.getName(), null);
        }
        List<User> users = Collections.singletonList(chatDialogUser);
        return new Dialog(document.getId(), null, chatDialogUser.getName(), users, message, 30);
    }

    private Message createMessageFromQueryDocumentSnapshot(QueryDocumentSnapshot document) {
        Map<String, Object> data = document.getData();
        User user = new User((String) data.get("senderUid"), null, null);

        Timestamp timestamp = (Timestamp) data.get(CREATED_AT);
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
                .orderBy(CREATED_AT, Query.Direction.DESCENDING)
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

    private interface OnDialogsCreatedListener {
        void onDialogsCreated(List<Dialog> dialogs);
    }

}
