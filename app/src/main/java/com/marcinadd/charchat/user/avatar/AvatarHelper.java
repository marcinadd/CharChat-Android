package com.marcinadd.charchat.user.avatar;

import android.app.Activity;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class AvatarHelper {
    private static final AvatarHelper ourInstance = new AvatarHelper();

    private AvatarHelper() {
    }

    public static AvatarHelper getInstance() {
        return ourInstance;
    }

    public void loadAvatarIntoImageView(String serverPath, final Activity activity, final ImageView imageView) {
        FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = firebaseStorage.getReference().child(serverPath);
        Glide.with(activity)
                .load(storageReference)
                .into(imageView);
    }
}
