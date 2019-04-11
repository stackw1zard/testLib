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
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
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
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
//import com.stackwizards.fire_wizard.R;
//import com.stackwizards.fire_wizard.entity.Questionaire;
//import com.stackwizards.fire_wizard.entity.WizardUser;
import com.stackwizards.mcq_wizard.entity.Questionaire;
import com.stackwizards.mcq_wizard.entity.WizardUser;

import java.io.IOException;
import java.util.UUID;

public class EditActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;

    ImageView imageView;
    EditText editText;
    EditText editUrlText;

    Uri uriProfileImage;
    ProgressBar progressBar;

    String profileImageUrl;

    FirebaseAuth mAuth;

    DatabaseReference dbRef;
    WizardUser wizardUser;


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
                if (wizardUser == null) {
//                    Toast.makeText(ProfileActivity.this, "zzz" + mAuth. , Toast.LENGTH_LONG).show();

                    userDataIsNull(dbRef);
                } else {
//                    editText.setText(wizardUser.getUsername());
//                    EditActivity.this.setTitle(wizardUser.getUsername());
//                    StorageReference mImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");
//
//                    if (mImageRef != null) {
//                        final long ONE_MEGABYTE = 1024 * 1024;
//                        mImageRef.getBytes(ONE_MEGABYTE)
//                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                                    @Override
//                                    public void onSuccess(byte[] bytes) {
//                                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                                        DisplayMetrics dm = new DisplayMetrics();
//                                        getWindowManager().getDefaultDisplay().getMetrics(dm);
//
//                                        imageView.setMinimumHeight(dm.heightPixels);
//                                        imageView.setMinimumWidth(dm.widthPixels);
//                                        imageView.setImageBitmap(bm);
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception exception) {
//                                // Handle any errors
//                            }
//                        });
//
//                    }

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
                // Attach a listener to read the data at our posts reference
//                Toast.makeText(ProfileActivity.this, "wizardUser.getBio()", Toast.LENGTH_SHORT).show();
//                saveUserInformation();
                uploadImageToFirebaseStorage();
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

                uploadImageToFirebaseStorage();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage() {


        String displayName = editText.getText().toString() + ".png";

        if (displayName.isEmpty()) {
            editText.setError("Name required");
            editText.requestFocus();
            return;
        }

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


        Questionaire questionaire = new Questionaire();
        questionaire.setIconName(displayName);
        questionaire.setJsonUrl(jsonUrl);

        String randomId = UUID.randomUUID().toString();


        dbRef = FirebaseDatabase.getInstance().getReference().child("Questionaires").child(randomId);

        dbRef.setValue(questionaire);

        final StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("questionaires/icons/" + displayName );

        if (uriProfileImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            profileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
                            Toast.makeText(EditActivity.this, displayName + " was uploaded", Toast.LENGTH_SHORT).show();
                            profileImageUrl = profileImageRef.getDownloadUrl().toString();
                            Bitmap bitmap = null;
                            try {
                                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                            imageView.setImageBitmap(bitmap);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressBar.setVisibility(View.GONE);

                            Toast.makeText(EditActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        getMenuInflater().inflate(R.menu.menu, menu);
//        MenuInterface.initMenu(menu, R.id.menuProfile);
//        return true;
//    }
//
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        MenuInterface.menuClickAction(this, item);
//        return true;
//    }
//
    private void showImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Profile Image"), CHOOSE_IMAGE);
    }


    private void userDataIsNull(DatabaseReference dbRefDefault) {
        WizardUser wizz = new WizardUser();
        wizz.setUsername("wizardName");
        wizz.setBio("Not much to say");

        dbRefDefault.setValue(wizz);
    }
}
