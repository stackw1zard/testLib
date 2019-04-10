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
import com.stackwizards.mcq_wizard.entity.WizardUser;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class ProfileActivity extends AppCompatActivity {

    private static final int CHOOSE_IMAGE = 101;

    TextView textView;
    ImageView imageView;
    EditText editText;

    Uri uriProfileImage;
    ProgressBar progressBar;

    String profileImageUrl;

    FirebaseAuth mAuth;

    DatabaseReference dbRef;
    DatabaseReference dbRefDefault2;
    WizardUser wizardUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_profile);
        FirebaseApp.initializeApp(this);

        editText = (EditText) findViewById(R.id.editTextDisplayName);
        imageView = (ImageView) findViewById(R.id.profile_icon);
        progressBar = (ProgressBar) findViewById(R.id.progressbar);
        textView = (TextView) findViewById(R.id.textViewVerified);

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showImageChooser();
            }
        });
//        wizardUser = new WizardUser();

        mAuth = FirebaseAuth.getInstance();
        dbRefDefault2 = FirebaseDatabase.getInstance().getReference().child("Members");
        dbRef = FirebaseDatabase.getInstance().getReference().child("Members").child(mAuth.getUid());


//        dbRef = FirebaseDatabase.getInstance().getReference().child("Members");
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wizardUser = dataSnapshot.getValue(WizardUser.class);
                if (wizardUser == null) {
//                    Toast.makeText(ProfileActivity.this, "zzz" + mAuth. , Toast.LENGTH_LONG).show();

                    userDataIsNull(dbRef);
                } else {
                    editText.setText(wizardUser.getUsername());
                    ProfileActivity.this.setTitle(wizardUser.getUsername());
                    StorageReference mImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");

                    if (mImageRef != null) {
                        final long ONE_MEGABYTE = 1024 * 1024;
                        mImageRef.getBytes(ONE_MEGABYTE)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                        DisplayMetrics dm = new DisplayMetrics();
                                        getWindowManager().getDefaultDisplay().getMetrics(dm);

                                        imageView.setMinimumHeight(dm.heightPixels);
                                        imageView.setMinimumWidth(dm.widthPixels);
                                        imageView.setImageBitmap(bm);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });

                    }

                }
//                String bio = (String) dataSnapshot.child("bio").getValue().toString();
//                wizardUser.setBio("xxx " + bio);
//                        String user = (String) dataSnapshot.child("username").getValue().toString();
//                wizardUser.setUsername("xxx " + user);

//                        Toast.makeText(ProfileActivity.this,bio, Toast.LENGTH_SHORT).show();

            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });

        loadUserInformation();

//        Toolbar toolbar = findViewById(R.id.toolbar);
//        setSupportActionBar(toolbar);




        findViewById(R.id.buttonUpdate).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Attach a listener to read the data at our posts reference

//                Toast.makeText(ProfileActivity.this, "wizardUser.getBio()", Toast.LENGTH_SHORT).show();
                saveUserInformation();
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

    private void loadUserInformation() {
        final FirebaseUser user = mAuth.getCurrentUser();

        if (null != user) {
//            if (user.getPhotoUrl() != null) {
//                Glide.with(this)
//                        .load(user.getPhotoUrl().toString())
//                        .into(imageView);
////                Toast.makeText(ProfileActivity.this, user.getPhotoUrl().toString(), Toast.LENGTH_LONG).show();
//// Load the image using Glide
//
//
//
//
//            }

            if (user.getDisplayName() != null) {
                editText.setText(user.getDisplayName());
            }

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


        } else {
            userDataIsNull(dbRef);
        }
    }


    private void saveUserInformation() {


        String displayName = editText.getText().toString();

        if (displayName.isEmpty()) {
            editText.setError("Name required");
            editText.requestFocus();
            return;
        }

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && profileImageUrl != null) {
            UserProfileChangeRequest profile = new UserProfileChangeRequest.Builder()
                    .setDisplayName(displayName)
                    .setPhotoUri(Uri.parse(profileImageUrl))
                    .build();

            user.updateProfile(profile)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()) {
                                Toast.makeText(ProfileActivity.this, "Profile Updated", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });


        }

        wizardUser.setUsername(displayName);
        dbRef.setValue(wizardUser);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CHOOSE_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            uriProfileImage = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uriProfileImage);

                Bitmap resized = Bitmap.createScaledBitmap(bitmap, 124, 124, true);


                imageView.setImageBitmap(resized);
                uploadBitmapFirebase(resized);
//                uploadImageToFirebaseStorage(resized);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void uploadImageToFirebaseStorage(Bitmap bitmap) {

        final StorageReference profileImageRef =
                FirebaseStorage.getInstance().getReference("profilepics/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");

        if (uriProfileImage != null) {
            progressBar.setVisibility(View.VISIBLE);
            profileImageRef.putFile(uriProfileImage)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressBar.setVisibility(View.GONE);
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

                            Toast.makeText(ProfileActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
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


    private void userDataIsNull(DatabaseReference dbRefDefault) {
        WizardUser wizz = new WizardUser();
        wizz.setUsername("wizardName");
//        wizz.setEmail("bla@b.com");
        wizz.setBio("Not much to say");
//        wizz.setAge(23);
////        wizz.notes.put("mt", 12);
////        wizz.notes.put("java", 2);
//        Map<String, Integer> notes = new HashMap<>();
//        notes.put("Starter", 1);
//       wizz.setNotes(notes);
//        dbRef.push().setValue(wizz);
        dbRefDefault.setValue(wizz);


    }


    private void uploadBitmapFirebase(Bitmap bitmap){

        if (bitmap != null) {
            progressBar.setVisibility(View.VISIBLE);

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] data = baos.toByteArray();

            final StorageReference profileImageRef =
                    FirebaseStorage.getInstance().getReference("profilepics/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");

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
                    progressBar.setVisibility(View.GONE);
//                    profileImageUrl = profileImageRef.getDownloadUrl().toString();

                }
            });

        }
    }

}



