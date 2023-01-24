package com.dan.timewebclone.providers;

import android.content.Context;
import android.net.Uri;

import com.dan.timewebclone.utils.CompressorBitmapImage;
import com.google.android.gms.tasks.Task;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.Date;

public class ImageProvider {

    private FirebaseStorage mFirebaseStorage;
    private StorageReference storageReference;

    //Instancia
    public ImageProvider() {
        mFirebaseStorage = FirebaseStorage.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();
    }

    //Guardar imagen bite en storage
    public UploadTask saveImage(Context context, byte[] image) {
        StorageReference storage = storageReference.child(new Date() + ".jpg");
        storageReference = storage;
        UploadTask task = storage.putBytes(image);
        return task;
    }


    //Guardar imagen file en storage
    public UploadTask saveImage(Context context,File file) {
        byte[] imageByte = CompressorBitmapImage.getImage(context, file.getPath(), 500, 500);
        StorageReference storage = storageReference.child(new Date() + ".jpg");
        storageReference = storage;
        UploadTask task = storage.putBytes(imageByte);
        return task;
    }

    //Obtener Url donde se guardo imagen
    public Task<Uri> getDownloadUri() {
        return storageReference.getDownloadUrl();
    }

    //Eliminar imagen
    public Task<Void> delete(String url) {
        return mFirebaseStorage.getReferenceFromUrl(url).delete();
    }

}
