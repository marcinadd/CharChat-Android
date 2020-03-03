package com.marcinadd.charchat.user;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.LinkedHashMap;
import java.util.Map;

public class UserService {
    private static final UserService ourInstance = new UserService();

    public static UserService getInstance() {
        return ourInstance;
    }

    private final static String USER_CREDENTIALS = "user_credentials";

    private UserService() {
    }

    public void checkIfCurrentUserHasUsername(final OnUserUsernameCheckDoneListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        db.collection(USER_CREDENTIALS)
                .document(firebaseUser.getUid())
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot documentSnapshot = task.getResult();
                            if (documentSnapshot.exists()) {
                                listener.onUsernameAlreadySet(documentSnapshot.getString("username"));
                            } else {
                                listener.onUsernameNotExisting();
                            }
                        }
                    }
                });
    }

    public void setUsernameForCurrentUser(final String username, final OnUserUsernameSet listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("username", username);

        db.collection(USER_CREDENTIALS)
                .document(firebaseUser.getUid())
                .set(data)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        listener.onSuccess(username);
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        listener.onFailure();
                    }
                });
    }

    void registerFCMToken() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String userId = FirebaseAuth.getInstance().getUid();
        FirebaseInstanceId.getInstance().getInstanceId()
                .addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
                    @Override
                    public void onComplete(@NonNull Task<InstanceIdResult> task) {
                        if (!task.isSuccessful())
                            return;
                        String token = task.getResult().getToken();
                        db.collection(USER_CREDENTIALS)
                                .document(userId.trim())
                                .update("tokens", FieldValue.arrayUnion(token));
                    }
                });
    }

    public interface OnUserUsernameCheckDoneListener {
        void onUsernameAlreadySet(String username);
        void onUsernameNotExisting();
    }

    public interface OnUserUsernameSet {
        void onSuccess(String username);
        void onFailure();
    }
}
