package com.marcinadd.charchat.chat.service;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marcinadd.charchat.chat.db.model.Chat;
import com.marcinadd.charchat.chat.db.model.ChatMessage;
import com.marcinadd.charchat.chat.model.Dialog;
import com.marcinadd.charchat.chat.model.Message;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.chat.service.listener.OnChatCreatedListener;
import com.marcinadd.charchat.chat.service.listener.OnChatsLoadedListener;
import com.marcinadd.charchat.chat.service.listener.OnDialogsLoadedListener;
import com.marcinadd.charchat.chat.service.listener.OnMessagesLoadedListener;
import com.marcinadd.charchat.chat.service.listener.OnNewChatMessageArrivedListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.marcinadd.charchat.chat.db.model.FieldNames.CHATS;
import static com.marcinadd.charchat.chat.db.model.FieldNames.CREATED_AT;
import static com.marcinadd.charchat.chat.db.model.FieldNames.CREATOR_HIDDEN;
import static com.marcinadd.charchat.chat.db.model.FieldNames.CREATOR_REFERENCE;
import static com.marcinadd.charchat.chat.db.model.FieldNames.RECEIVER_REFERENCE;
import static com.marcinadd.charchat.chat.db.model.FieldNames.USER_CREDENTIALS;

public class ChatService {
    private static final ChatService ourInstance = new ChatService();

    private ChatService() {
    }

    public static ChatService getInstance() {
        return ourInstance;
    }

    public void createNewChat(final String userUid, final String username, final OnChatCreatedListener listener) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference referenceCreator = db.collection(USER_CREDENTIALS.toString()).document(firebaseUser.getUid());
        DocumentReference referenceReceiver = db.collection(USER_CREDENTIALS.toString()).document(userUid.trim());
        Map<String, Object> chat = ChatHelper.getInstance().createNewChatMap(referenceCreator, referenceReceiver, username);

        db.collection(CHATS.toString())
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
        DocumentReference currentUser = db.collection(USER_CREDENTIALS.toString()).document(firebaseUser.getUid());
        DocumentReference otherUser = db.collection(USER_CREDENTIALS.toString()).document(otherUserUid.trim());

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        final Task<QuerySnapshot> taskA = db.collection(CHATS.toString())
                .whereEqualTo(CREATOR_REFERENCE.toString(), otherUser)
                .whereEqualTo(RECEIVER_REFERENCE.toString(), currentUser)
                .whereEqualTo(CREATOR_HIDDEN.toString(), false)
                .get();

        final Task<QuerySnapshot> taskB = db.collection(CHATS.toString())
                .whereEqualTo(CREATOR_REFERENCE.toString(), currentUser)
                .whereEqualTo(RECEIVER_REFERENCE.toString(), otherUser)
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
                                    ChatHelper.getInstance().createChatFromMap(document.getData(), document.getId())
                            );

                        }

                        for (QueryDocumentSnapshot document : taskB.getResult()) {
                            chats.add(
                                    ChatHelper.getInstance().createChatFromMap(document.getData(), document.getId())
                            );
                        }
                        listener.onChatsLoaded(chats, otherUserUid, otherUserUsername);
                    }
                });
    }

    public void getChats(final OnDialogsLoadedListener listener) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final ChatHelper chatHelper = ChatHelper.getInstance();
        DocumentReference userReference = db.collection(USER_CREDENTIALS.toString()).document(firebaseUser.getUid());

        final Task<QuerySnapshot> taskA = db.collection(CHATS.toString())
                .whereEqualTo(RECEIVER_REFERENCE.toString(), userReference)
                .get();

        final Task<QuerySnapshot> taskB = db.collection(CHATS.toString())
                .whereEqualTo(CREATOR_REFERENCE.toString(), userReference)
                .get();


        Tasks.whenAll(taskA, taskB)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        final List<Dialog> dialogList = new ArrayList<>();
                        String currentUserId = firebaseUser.getUid();
                        List<Chat> chats = new ArrayList<>();
                        if (taskA.getResult() != null)
                            chats.addAll(chatHelper.createChatsFromQuerySnapshot(taskA.getResult()));
                        if (taskB.getResult() != null)
                            chats.addAll(chatHelper.createChatsFromQuerySnapshot(taskB.getResult()));
                        createDialogs(chats, currentUserId, db, onDialogsCreatedListener(dialogList, listener));
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


    private void createDialogs(final List<Chat> chats, final String currentUserId, FirebaseFirestore db, final OnDialogsCreatedListener listener) {
        final List<Dialog> dialogs = new ArrayList<>();
        List<Task<?>> tasksQuery = new ArrayList<>();
        final ChatHelper chatHelper = ChatHelper.getInstance();
        for (final Chat chat : chats) {
            List<Task<?>> thisChatTasks = new ArrayList<>();
            final Task<QuerySnapshot> getLastMessageTask = db.collection(getMessagesCollectionPath(chat.getId()))
                    .orderBy(CREATED_AT.toString(), Query.Direction.DESCENDING)
                    .limit(1)
                    .get();
            thisChatTasks.add(getLastMessageTask);

            boolean isCreatorCurrentUser = chat.getCreator().getId().equals(currentUserId);
            final String otherUserId = isCreatorCurrentUser ? chat.getReceiver().getId() : chat.getCreator().getId();

            Task<DocumentSnapshot> getOtherUserDataTask = null;
            if (isCreatorCurrentUser || !chat.isCreatorHidden()) {
                getOtherUserDataTask = db.collection(USER_CREDENTIALS.toString())
                        .document(otherUserId)
                        .get();
                thisChatTasks.add(getOtherUserDataTask);
            }

            tasksQuery.addAll(thisChatTasks);

            final Task<DocumentSnapshot> finalGetOtherUserDataTask = getOtherUserDataTask;
            Tasks.whenAll(thisChatTasks)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            List<ChatMessage> chatMessages = getLastMessageTask.getResult().toObjects(ChatMessage.class);
                            DocumentSnapshot userSnapshot = finalGetOtherUserDataTask != null ? finalGetOtherUserDataTask.getResult() : null;
                            User user = new User(otherUserId, null, null);
                            if (userSnapshot != null && userSnapshot.getData() != null) {
                                user = ChatHelper.getInstance().createUserFromMap(userSnapshot.getData());
                                user.setId(finalGetOtherUserDataTask.getResult().getId());
                            }
                            ChatMessage lastMessage = chatMessages.size() > 0 ? chatMessages.get(0) : null;
                            Dialog dialog = chatHelper.createDialogFromObjects(chat, user, lastMessage);
                            dialogs.add(dialog);

                        }
                    });

        }

        Tasks.whenAll(tasksQuery)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        listener.onDialogsCreated(dialogs);
                    }
                });
    }

    public void sendMessage(Message message, String chatId, String recipientId) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(getMessagesCollectionPath(chatId))
                .document().set(new ChatMessage(message, recipientId));
    }

    public void getMessagesForSpecificChat(String chatId, final OnMessagesLoadedListener listener) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(getMessagesCollectionPath(chatId))
                .orderBy(CREATED_AT.toString(), Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    List<Message> messages = new ArrayList<>();

                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            Message message = ChatHelper.getInstance().createMessageFromMap(document.getData(), document.getId());
                            messages.add(message);
                        }
                        listener.onMessagesLoaded(messages);
                    }
                });
    }

    public ListenerRegistration listenToNewMessages(String chatId, final OnNewChatMessageArrivedListener listener) {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String currentUserUid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        CollectionReference collection = db.collection(getMessagesCollectionPath(chatId));
        final AtomicBoolean isFirst = new AtomicBoolean(true);
        return collection.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot querySnapshot, @Nullable FirebaseFirestoreException e) {
                if (e != null || isFirst.get()) {
                    isFirst.set(false);
                    return;
                }
                for (DocumentChange dc : querySnapshot.getDocumentChanges()) {
                    if (dc.getType().equals(DocumentChange.Type.ADDED)) {
                        ChatMessage message = dc.getDocument().toObject(ChatMessage.class);
                        message.setId(dc.getDocument().getId());
                        if (!message.getSenderUid().equals(currentUserUid))
                            listener.onNewChatMessageArrived(message);
                    }
                }
            }
        });
    }

    private String getMessagesCollectionPath(String chatId) {
        return CHATS.toString() + "/" + (chatId.trim()) + "/messages";
    }


    private interface OnDialogsCreatedListener {
        void onDialogsCreated(List<Dialog> dialogs);
    }

}
