package com.stackwizards.mcq_wizard;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.stackwizards.mcq_wizard.entity.WizardUser;
import com.stackwizards.mcq_wizard.fragment.DownloadFragment;
import com.stackwizards.mcq_wizard.fragment.HelpInfoFragment;
import com.stackwizards.mcq_wizard.fragment.LeaderBoardFragment;
import com.stackwizards.mcq_wizard.fragment.OfflineFragment;
import com.stackwizards.mcq_wizard.fragment.OnlineFragment;
import com.stackwizards.mcq_wizard.utils.FirebaseServiceUtils;

import static com.stackwizards.mcq_wizard.utils.FirebaseServiceUtils.uploadProfilePictureToFirebase;
import static com.stackwizards.mcq_wizard.utils.GraphicsUtils.getRoundedCornerBitmap;
import static com.stackwizards.mcq_wizard.utils.StorageUtils.isExternalStorageAvailable;
import static com.stackwizards.mcq_wizard.utils.StorageUtils.isExternalStorageReadOnly;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    private FirebaseAuth mAuth;
    private DatabaseReference dbRef;
    private WizardUser wizardUser;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            navigationView.findViewById(R.id.nav_download).setVisibility(View.GONE);
        }

        mAuth = FirebaseAuth.getInstance();

        loadFragment(new HelpInfoFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        syncWizardUser();
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

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case R.id.nav_online:
                loadFragment(new OnlineFragment());
                break;
            case R.id.nav_offline:
                loadFragment(new OfflineFragment());
                break;
            case R.id.nav_download:
                loadFragment(new DownloadFragment());
                break;
            case R.id.nav_manual:
                loadFragment(new HelpInfoFragment());
                break;
            case R.id.nav_leader_board:
                loadFragment(new LeaderBoardFragment());
                break;
            case R.id.nav_profile:
                startActivity(new Intent(this, ProfileActivity.class));
                break;
            case R.id.nav_add_mcq:
                startActivity(new Intent(this, AddQuestionaireActivity.class));
                break;
            case R.id.nav_logout:
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, LoginActivity.class));
                break;
        }

        return true;
    }


    private void loadFragment(Fragment fragment) {
        FragmentManager fm = getFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }

    private void initialiseNewUser(DatabaseReference dbRefDefault) {
        FirebaseServiceUtils.userDataIsNull(mAuth, dbRefDefault);

        String name = mAuth.getCurrentUser().getEmail().split("@")[0];
        ((TextView) findViewById(R.id.userName)).setText(name);
        ((TextView) findViewById(R.id.userEmail)).setText(mAuth.getCurrentUser().getEmail());

        Bitmap defaultProfilPicture = BitmapFactory.decodeResource(this.getResources(), R.drawable.android_plain_wordmark);
        uploadProfilePictureToFirebase(mAuth, defaultProfilPicture, null);
        ((ImageView) findViewById(R.id.profileImageView)).setImageBitmap(getRoundedCornerBitmap(defaultProfilPicture, 10));
    }

    private void syncWizardUser() {
        dbRef = FirebaseDatabase.getInstance().getReference().child("Members").child(mAuth.getUid());
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wizardUser = dataSnapshot.getValue(WizardUser.class);
                if (wizardUser == null) {
                    initialiseNewUser(dbRef);
                } else {
                    Menu nav_Menu = navigationView.getMenu();
                    // Allow user to upload questionaires if role is set to 7
                    if (wizardUser.getRole() == 7) {
                        nav_Menu.findItem(R.id.nav_add_mcq).setVisible(true);
                    } else {
                        nav_Menu.findItem(R.id.nav_add_mcq).setVisible(false);
                    }
                    loadApplicationDrawer();
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });
    }

    private void loadApplicationDrawer() {
        StorageReference profileImageReference = FirebaseStorage.getInstance().getReference("profilepics/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");

        if (profileImageReference != null) {
            final long ONE_MEGABYTE = 1024 * 1024;
            profileImageReference.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap profileImage = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                            ImageView imageView = findViewById(R.id.profileImageView);
                            profileImage = getRoundedCornerBitmap(profileImage, 10);
                            imageView.setImageBitmap(profileImage);

                            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                            TextView userName = ((TextView) drawer.findViewById(R.id.userName));
                            if (userName != null && wizardUser != null && wizardUser.getUsername() != null)
                                userName.setText(wizardUser.getUsername());
                            TextView emailTextView = ((TextView) findViewById(R.id.userEmail));
                            if (emailTextView != null)
                                emailTextView.setText(mAuth.getCurrentUser().getEmail());
                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });
        }

        DrawerLayout drawer = ((DrawerLayout) findViewById(R.id.drawer_layout));
        drawer.openDrawer(GravityCompat.START);
    }

}
