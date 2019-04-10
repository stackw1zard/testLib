package com.stackwizards.mcq_wizard;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.util.DisplayMetrics;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
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


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth mAuth;

    DatabaseReference dbRef;
    WizardUser wizardUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);


        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);













        mAuth = FirebaseAuth.getInstance();

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
//                    editText.setText(wizardUser.getUsername());
//                    ProfileActivity.this.setTitle(wizardUser.getUsername());
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
                                        ImageView imageView = findViewById(R.id.profileImageView);
//                                        imageView.setMinimumHeight(dm.heightPixels);
//                                        imageView.setMinimumWidth(dm.widthPixels);
                                        imageView.setImageBitmap(bm);

                                        ((TextView)findViewById(R.id.userName)).setText(wizardUser.getUsername());
                                        ((TextView)findViewById(R.id.userEmail)).setText( mAuth.getCurrentUser().getEmail());
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


    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_online) {
            // Handle the camera action
            loadFragment(new SimpleFragment2());

        } else if (id == R.id.nav_offline) {
            loadFragment(new SimpleFragment());

        } else if (id == R.id.nav_download) {

        } else if (id == R.id.nav_profile) {
            startActivity(new Intent(this, ProfileActivity.class));
        } else if (id == R.id.nav_add_mcq) {
//            finish();
            startActivity(new Intent(this, EditActivity.class));
        } else if (id == R.id.nav_logout) {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(this, LoginActivity.class));
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }



    private void loadFragment(Fragment fragment) {
// create a FragmentManager
        FragmentManager fm = getFragmentManager();
// create a FragmentTransaction to begin the transaction and replace the Fragment
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
// replace the FrameLayout with new Fragment
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit(); // save the changes
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
}
