package com.stackwizards.mcq_wizard;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stackwizards.mcq_wizard.entity.Questionaire;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class AddQuestionnaireActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;

    private Bitmap questionnaireIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_edit);
        FirebaseApp.initializeApp(this); //re-establish AUTH database connection

        getQuestionImageView().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });

        findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (questionnaireIcon != null) {
                    prepareQuestionnaireForUpload();
                } else {
                    Toast.makeText(AddQuestionnaireActivity.this, "Bitmap is empty...", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (FirebaseAuth.getInstance().getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    /* Start activity to get icon for questionnaire */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Uri uriProfileImage;

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                getQuestionImageView().setImageBitmap(bitmap);
                // This questionnaireIcon will be used as icon for questionaire and do not need to be of high quality
                this.questionnaireIcon = Bitmap.createScaledBitmap(bitmap, 124, 124, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private ImageView getQuestionImageView() {
        return (ImageView) findViewById(R.id.profile_icon);
    }

    private String getEditTextFieldValue(int rid) {
        EditText editText = (EditText) findViewById(rid);
        String text = editText.getText().toString();
        if (text.isEmpty()) {
            editText.setError("Field required");
            editText.requestFocus();
            return "";
        }
        return text;
    }

    private void prepareQuestionnaireForUpload() {

        String jsonUrl = getEditTextFieldValue(R.id.editTextJsonUrl);
        if (jsonUrl.isEmpty() || getEditTextFieldValue(R.id.editTextDisplayName).isEmpty()) {
            return;
        }
        String displayName = getEditTextFieldValue(R.id.editTextDisplayName) + ".png";

        findViewById(R.id.progressbar).setVisibility(View.VISIBLE);

        final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("questionaires/icons/" + displayName);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        questionnaireIcon.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        UploadTask uploadTask = profileImageRef.putBytes(baos.toByteArray());

        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle unsuccessful uploads
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                uploadQuestionnaire(displayName, jsonUrl);
                findViewById(R.id.progressbar).setVisibility(View.GONE);
            }
        });

    }

    private void uploadQuestionnaire(String displayName, String jsonUrl) {
        Questionaire questionaire = new Questionaire();
        questionaire.setIconName(displayName);
        questionaire.setJsonUrl(jsonUrl);

        String randomId = UUID.randomUUID().toString();
        DatabaseReference dbRef = FirebaseDatabase.getInstance().getReference().child("Questionaires").child(randomId);
        dbRef.setValue(questionaire);
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

}
