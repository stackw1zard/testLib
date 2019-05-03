package com.stackwizards.mcq_wizard;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stackwizards.mcq_wizard.entity.WizardUser;
import com.stackwizards.mcq_wizard.utils.FirebaseServiceUtils;

import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 107;

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private WizardUser wizardUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_profile);
        FirebaseApp.initializeApp(this); // re-establish connection to database

        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference().child("Members").child(mAuth.getUid());
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wizardUser = dataSnapshot.getValue(WizardUser.class);
                if (wizardUser != null) {
                    getUserNameTextField().setText(wizardUser.getUsername());
                    ProfileActivity.this.setTitle(wizardUser.getUsername());
                    StorageReference profileImageReference = FirebaseStorage.getInstance().getReference("profilepics/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");

                    if (profileImageReference != null) {
                        final long ONE_MEGABYTE = 1024 * 1024;
                        profileImageReference.getBytes(ONE_MEGABYTE)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap wizardProfileImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        getUserProfileImage().setImageBitmap(wizardProfileImage);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                updateWizardName();
            }
        });

        getUserProfileImage().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });

        loadWizardInformation();

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
        Uri uriProfileImage;

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                Bitmap scaledBitmap = Bitmap.createScaledBitmap(bitmap, 124, 124, true);
                getUserProfileImage().setImageBitmap(scaledBitmap);
                FirebaseServiceUtils.uploadProfilePictureToFirebase(mAuth, scaledBitmap, (ProgressBar) findViewById(R.id.progressbar));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    private EditText getUserNameTextField() {
        return (EditText) findViewById(R.id.editTextDisplayName);
    }

    private ImageView getUserProfileImage() {
        return (ImageView) findViewById(R.id.profile_icon);
    }

    private void updateWizardName() {
        EditText userNameField = getUserNameTextField();
        String newName = userNameField.getText().toString();
        if (newName.isEmpty()) {
            userNameField.setError("Name required");
            userNameField.requestFocus();
            return;
        }
        wizardUser.setUsername(newName);
        dbRef.setValue(wizardUser);
    }

    private void loadWizardInformation() {
        final FirebaseUser user = mAuth.getCurrentUser();

        if (user != null) {
            if (null != user.getDisplayName()) {
                getUserNameTextField().setText(user.getDisplayName());
            }
            TextView textView = (TextView) findViewById(R.id.textViewVerified);
            if (user.isEmailVerified()) {
                textView.setText("Email Verified");
            } else {
                textView.setText("Email Not Verified (Click to Verify)");
                textView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                Toast.makeText(ProfileActivity.this, "Verification Email Sent", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                });
            }
        }
    }

    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }

}



