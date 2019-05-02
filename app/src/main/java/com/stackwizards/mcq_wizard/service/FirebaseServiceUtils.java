package com.stackwizards.mcq_wizard.service;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stackwizards.mcq_wizard.entity.WizardUser;

import java.io.ByteArrayOutputStream;

public class FirebaseServiceUtils {

    public static void uploadProfilePictureToFirebase(FirebaseAuth auth, Bitmap bitmap, ProgressBar progressBar) {
        if (bitmap != null) {
            if (progressBar != null) progressBar.setVisibility(View.VISIBLE);
            final StorageReference profileImageRef =
                    FirebaseStorage.getInstance().getReference("profilepics/" + auth.getCurrentUser().getUid() + "/profile.jpg");

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            UploadTask uploadTask = profileImageRef.putBytes(baos.toByteArray());

            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // notify successful uploads
                    if (progressBar != null) progressBar.setVisibility(View.GONE);
                }
            });
        }
    }


    public static void userDataIsNull(  FirebaseAuth auth, DatabaseReference dbRefDefault ) {
        FirebaseUser muser = auth.getCurrentUser();
        String name = muser.getEmail().split("@")[0];
        WizardUser wizz = new WizardUser();
        wizz.setUsername(name);
        wizz.setBio("I could say something about myself ..");
        dbRefDefault.setValue(wizz);
    }

}
