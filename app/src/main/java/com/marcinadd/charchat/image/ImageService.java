package com.marcinadd.charchat.image;

import android.util.Log;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.UUID;

public class ImageService {
    private static final ImageService ourInstance = new ImageService();

    private ImageService() {
    }

    public static ImageService getInstance() {
        return ourInstance;
    }

    public void uploadImageByPath(String path) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference currentImageRef = storageRef.child("images/" + UUID.randomUUID().toString());
        byte[] data = ImageHelper.getInstance().getImageAsByteArray(path);
        UploadTask uploadTask = currentImageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Log.e("ABCD!", taskSnapshot.getMetadata().getPath());
            }
        });
    }
}
