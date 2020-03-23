package com.marcinadd.charchat.user;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.marcinadd.charchat.chat.db.model.FieldNames;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.chat.service.ChatHelper;

import java.util.ArrayList;
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
                            if (documentSnapshot != null && documentSnapshot.getString(FieldNames.USERNAME.toString()) != null) {
                                listener.onUsernameAlreadySet(documentSnapshot.getString(FieldNames.USERNAME.toString()));
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
        data.put(FieldNames.USERNAME.toString(), username);
        if (firebaseUser != null) {
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
                                .update(FieldNames.TOKENS.toString(), FieldValue.arrayUnion(token));
                    }
                });
    }

    public void deleteFCMTokenFromDB() {
        final FirebaseFirestore db = FirebaseFirestore.getInstance();
        final String userId = FirebaseAuth.getInstance().getUid();
        final DocumentReference docRef = db.collection(USER_CREDENTIALS).document(userId);
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull final Task<InstanceIdResult> task) {
                if (!task.isSuccessful())
                    return;
                db.runTransaction(new Transaction.Function<Void>() {
                    @Nullable
                    @Override
                    public Void apply(@NonNull Transaction transaction) throws FirebaseFirestoreException {
                        Map<String, Object> data = transaction.get(docRef).getData();
                        ArrayList<String> tokens = (ArrayList<String>) data.get(FieldNames.TOKENS.toString());
                        if (tokens != null && task.getResult() != null) {
                            tokens.remove(task.getResult().getToken());
                        }
                        transaction.update(docRef, FieldNames.TOKENS.toString(), tokens);
                        return null;
                    }
                });
            }
        });
    }

    public void getUserCredentials(String uid, final OnUserCredentialsLoadedListener listener) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(USER_CREDENTIALS)
                .document(uid)
                .get()
                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        DocumentSnapshot userDocument = task.getResult();
                        if (userDocument != null && userDocument.getData() != null) {
                            User user = ChatHelper.getInstance().createUserFromMap(userDocument.getData());
                            listener.onUserCredentialsLoaded(user);
                        }

                    }
                });
    }

    public interface OnUserCredentialsLoadedListener {
        void onUserCredentialsLoaded(User user);
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
