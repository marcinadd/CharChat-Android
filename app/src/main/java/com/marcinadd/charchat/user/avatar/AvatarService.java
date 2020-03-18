package com.marcinadd.charchat.user.avatar;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.marcinadd.charchat.chat.db.model.FieldNames;
import com.marcinadd.charchat.chat.model.User;
import com.marcinadd.charchat.image.ImageService;
import com.marcinadd.charchat.image.listener.OnImageDeletedListener;

import java.util.LinkedHashMap;
import java.util.Map;

public class AvatarService {
    private static final AvatarService ourInstance = new AvatarService();

    private AvatarService() {
    }

    public static AvatarService getInstance() {
        return ourInstance;
    }


    public void updateAvatarInUserCredentials(final String avatarPath, final OnAvatarUpdatedListener listener) {
        String uid = FirebaseAuth.getInstance().getUid();
        Map<String, Object> map = new LinkedHashMap<>();
        map.put(FieldNames.AVATAR.toString(), avatarPath);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        if (uid != null) {
            db.collection(FieldNames.USER_CREDENTIALS.toString())
                    .document(uid)
                    .update(map)
                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            listener.onAvatarUpdated(avatarPath);
                        }
                    });
        }
    }

    public void deleteCurrentAvatarImageFromServer(final OnAvatarDeletedListener listener) {
        String uid = FirebaseAuth.getInstance().getUid();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection(FieldNames.USER_CREDENTIALS.toString())
                .document(uid)
                .get()
                .addOnCompleteListener(onUserCredentialsLoaded(listener));
    }

    OnCompleteListener<DocumentSnapshot> onUserCredentialsLoaded(final OnAvatarDeletedListener listener) {
        return new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful() && task.getResult() != null) {
                    User user = task.getResult().toObject(User.class);
                    if (user != null && user.getAvatar() != null) {
                        ImageService.getInstance().deleteImageByRemotePath(user.getAvatar(), new OnImageDeletedListener() {
                            @Override
                            public void onImageDeleted() {
                                listener.onAvatarDeleted();
                            }
                        });
                    } else {
                        listener.onAvatarDeleted();
                    }
                }
            }
        };
    }


    public interface OnAvatarUpdatedListener {
        void onAvatarUpdated(String newPath);
    }

    public interface OnAvatarDeletedListener {
        void onAvatarDeleted();
    }
}
