package com.marcinadd.charchat.image;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import java.io.ByteArrayOutputStream;

public class ImageHelper {
    private static final ImageHelper ourInstance = new ImageHelper();

    private ImageHelper() {
    }

    public static ImageHelper getInstance() {
        return ourInstance;
    }

    byte[] getImageAsByteArray(String imagePath) {
        Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        return baos.toByteArray();
    }
}
