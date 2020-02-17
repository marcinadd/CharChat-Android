package com.marcinadd.charchat.chat.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
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

    public void createNewChat(String username) {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();

        getUserUId(username, db, new Callback() {
            @Override
            public void onSuccess(String uid) {
                Map<String, Object> sampleChat = new HashMap<>();

                DocumentReference referenceCreator = db.collection(USER_CREDENTIALS).document(firebaseUser.getUid());
                DocumentReference referenceReceiver = db.collection(USER_CREDENTIALS).document(uid.trim());

                sampleChat.put("creator", referenceCreator);
                sampleChat.put("receiver", referenceReceiver);
                sampleChat.put("creator_hidden", true);
                db.collection(CHATS).document()
                        .set(sampleChat)
                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                            @Override
                            public void onSuccess(Void aVoid) {
                                Log.e("Succcess", "success");
                            }
                        });
            }

            @Override
            public void onFailed() {

            }
        });

    }

    private void getUserUId(String username, FirebaseFirestore db, final Callback callback) {
        Query receiverUid = db.collection(USER_CREDENTIALS).whereEqualTo(USERNAME, username);
        receiverUid.get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                callback.onSuccess(document.getId());
                            }

                        } else {
                            callback.onFailed();
                        }
                    }
                });
    }


    public void getChats() {
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference userReference = db.collection(USER_CREDENTIALS).document(firebaseUser.getUid());


        final Task<QuerySnapshot> taskA = db.collection(CHATS)
                .whereEqualTo(RECEIVER, userReference)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {

                            Log.e("SIZE!!", String.valueOf(task.getResult().size()));
                        } else {

                        }
                    }
                });

        final Task<QuerySnapshot> taskB = db.collection(CHATS)
                .whereEqualTo(CREATOR, userReference)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful() && task.getResult() != null) {

                            Log.e("SIZE!!", String.valueOf(task.getResult().size()));
                        } else {

                        }
                    }
                });

        List<Task<QuerySnapshot>> tasks = new ArrayList<>();

        tasks.add(taskA);
        tasks.add(taskB);

        Tasks.whenAll(tasks)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Log.e("Tasks", "Completeeed");
                        Log.e("SIZE!!", String.valueOf(taskA.getResult().size()));
                        Log.e("SIZE!!", String.valueOf(taskB.getResult().size()));
                        for (QueryDocumentSnapshot document : taskA.getResult()
                        ) {
                            Log.e("aaa", String.valueOf(document.getData()));
                        }
                        for (QueryDocumentSnapshot document : taskB.getResult()
                        ) {
                            Log.e("aaa", String.valueOf(document.getData()));
                        }
                    }
                });
    }


    interface Callback {
        void onSuccess(String uid);

        void onFailed();
    }

}
