package com.marcinadd.charchat.people.service;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.marcinadd.charchat.chat.model.User;

import java.util.ArrayList;
import java.util.List;

public class PeopleService {
    private static final String USER_CREDENTIALS = "user_credentials";
    private static final String USERNAME = "username";

    private static final PeopleService ourInstance = new PeopleService();

    public static PeopleService getInstance() {
        return ourInstance;
    }


    public void queryUsersByUsernamePart(String usernamePart, final OnPeopleSearchLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        usernamePart = usernamePart.toLowerCase();
        db.collection(USER_CREDENTIALS)
                .whereGreaterThanOrEqualTo(USERNAME, usernamePart)
                .whereLessThanOrEqualTo(USERNAME, usernamePart + '\uf8ff')
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        List<User> users = new ArrayList<>();
                        for (QueryDocumentSnapshot documentSnapshot : task.getResult()) {
                            users.add(createUserFormQueryDocumentSnapshot(documentSnapshot));
                        }
                        listener.onPeopleLoadedListener(users);
                    }
                });
    }

    private User createUserFormQueryDocumentSnapshot(QueryDocumentSnapshot document) {
        return new User(document.getId(), (String) document.get(USERNAME), null);
    }
}
