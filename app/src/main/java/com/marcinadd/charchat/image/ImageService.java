package com.marcinadd.charchat.image;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.marcinadd.charchat.image.listener.OnImageDeletedListener;
import com.marcinadd.charchat.image.listener.OnImageUploadedListener;

import java.util.UUID;

public class ImageService {
    private static final ImageService ourInstance = new ImageService();

    private ImageService() {
    }

    public static ImageService getInstance() {
        return ourInstance;
    }

    public void uploadImageByPath(String path, String serverDirectory, final OnImageUploadedListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference currentImageRef = storageRef.child(serverDirectory + "/" + UUID.randomUUID().toString());
        byte[] data = ImageHelper.getInstance().getImageAsByteArray(path);
        UploadTask uploadTask = currentImageRef.putBytes(data);
        uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (listener != null)
                    listener.onImageUploaded(taskSnapshot.getMetadata().getPath());
            }
        });
    }

    public void deleteImageByRemotePath(String remotePath, final OnImageDeletedListener listener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();

        StorageReference imageReference = storageRef.child(remotePath);
        imageReference.delete()
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        if (listener != null) listener.onImageDeleted();
                    }
                });
    }

}
