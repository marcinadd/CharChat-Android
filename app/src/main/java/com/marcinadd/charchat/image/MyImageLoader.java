package com.marcinadd.charchat.image;

import android.app.Activity;
import android.widget.ImageView;

import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stfalcon.chatkit.commons.ImageLoader;

public class MyImageLoader implements ImageLoader {

    private Activity activity;

    public MyImageLoader(Activity activity) {
        this.activity = activity;
    }

    @Override
    public void loadImage(ImageView imageView, @Nullable String url, @Nullable Object payload) {
        final FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
        StorageReference storageReference = null;
        if (url != null) {
            storageReference = firebaseStorage.getReference().child(url);
        }
        Glide.with(activity)
                .load(storageReference)
                .into(imageView);
    }
}
