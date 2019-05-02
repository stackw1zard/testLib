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
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.stackwizards.mcq_wizard.entity.Questionaire;
import com.stackwizards.mcq_wizard.entity.WizardUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

public class UploadQuestionaireActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;

    ImageView imageView;
    EditText editText;
    EditText editUrlText;

    Uri uriProfileImage;
    ProgressBar progressBar;
    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    WizardUser wizardUser;
    Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_edit);
        FirebaseApp.initializeApp(this);

        editText = (EditText) findViewById(R.id.editTextDisplayName);
        editUrlText = findViewById(R.id.editTextJsonUrl);
        imageView = (ImageView) findViewById(R.id.profile_icon);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });

        mAuth = FirebaseAuth.getInstance();

        dbRef = FirebaseDatabase.getInstance().getReference().child("Members");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wizardUser = dataSnapshot.getValue(WizardUser.class);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (bitmap != null) {
                    uploadQuestionaireToFirebaseStorage(bitmap);
                }else {
                    Toast.makeText(UploadQuestionaireActivity.this, "Bitmap is empty...", Toast.LENGTH_LONG).show();
                }
            }
        });

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (mAuth.getCurrentUser() == null) {
            finish();
            startActivity(new Intent(this, MainActivity.class));
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                imageView.setImageBitmap(bitmap);
                // This bitmap will be used as icon for questionaire and do not need to be of high quality
                this.bitmap =  Bitmap.createScaledBitmap(bitmap, 124, 124, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private void uploadQuestionaireToFirebaseStorage(Bitmap bitmap) {

        String displayName = editText.getText().toString() + ".png";
        if (displayName.isEmpty()) {
            editText.setError("Name required");
            editText.requestFocus();
            return;
        }

        String jsonUrl = editUrlText.getText().toString();
        if (jsonUrl.isEmpty()) {
            editUrlText.setError("Json Url required");
            editUrlText.requestFocus();
            return;
        }

        if (bitmap != null) {
            progressBar.setVisibility(View.VISIBLE);
            final StorageReference profileImageRef = FirebaseStorage.getInstance().getReference("questionaires/icons/" + displayName );
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();
            UploadTask uploadTask = profileImageRef.putBytes(data);
            uploadTask.addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle unsuccessful uploads
                }
            }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                    Questionaire questionaire = new Questionaire();
                    questionaire.setIconName(displayName);
                    questionaire.setJsonUrl(jsonUrl);

                    String randomId = UUID.randomUUID().toString();
                    dbRef = FirebaseDatabase.getInstance().getReference().child("Questionaires").child(randomId);
                    dbRef.setValue(questionaire);

                    progressBar.setVisibility(View.GONE);
                }
            });

        }

    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }


}
