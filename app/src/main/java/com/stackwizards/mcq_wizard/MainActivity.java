package com.stackwizards.mcq_wizard;

import android.content.ClipData;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.view.menu.MenuView;
import android.util.DisplayMetrics;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;


import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
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
import com.google.firebase.storage.UploadTask;
import com.stackwizards.mcq_wizard.entity.WizardUser;
import com.stackwizards.mcq_wizard.fragment.DownloadFragment;
import com.stackwizards.mcq_wizard.fragment.HelpInfoFragment;
import com.stackwizards.mcq_wizard.fragment.LeaderBoardFragment;
import com.stackwizards.mcq_wizard.fragment.OfflineFragment;
import com.stackwizards.mcq_wizard.fragment.OnlineFragment;

import java.io.ByteArrayOutputStream;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener {

    FirebaseAuth mAuth;
    TextView userName;

    DatabaseReference dbRef;
    WizardUser wizardUser;
    private RequestQueue mQueue;
    private NavigationView navigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
//            mView.findViewById(R.id.saveExternalStorage).setEnabled(false);
            navigationView.findViewById(R.id.nav_download).setVisibility(View.GONE);
        }


        mQueue = Volley.newRequestQueue(this);


        mAuth = FirebaseAuth.getInstance();

//        getFireUser();

        loadFragment(new HelpInfoFragment());
//        View drawer = findViewById(R.id.nav_view);
//
//        ((MenuItem)drawer.findViewById(R.id.nav_add_mcq)).setVisible(true);

    }

    private void getFireUser() {
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
                    Menu nav_Menu = navigationView.getMenu();
                    if(wizardUser.getRole()==7){
                        nav_Menu.findItem(R.id.nav_add_mcq).setVisible(true);
                    }else{
                        nav_Menu.findItem(R.id.nav_add_mcq).setVisible(false);
                    }
                    loadUserDrawer();

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

    private void loadUserDrawer() {
        StorageReference mImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + mAuth.getCurrentUser().getUid() + "/profile.jpg");

        if (mImageRef != null) {
            final long ONE_MEGABYTE = 1024 * 1024;
            mImageRef.getBytes(ONE_MEGABYTE)
                    .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                        @Override
                        public void onSuccess(byte[] bytes) {
                            Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                            DisplayMetrics dm = new DisplayMetrics();
//                            getWindowManager().getDefaultDisplay().getMetrics(dm);
                            ImageView imageView = findViewById(R.id.profileImageView);
//                                        imageView.setMinimumHeight(dm.heightPixels);
//                                        imageView.setMinimumWidth(dm.widthPixels);
                            bm = getRoundedCornerBitmap(bm, 10);

                            imageView.setImageBitmap(bm);



                            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
                            userName = ((TextView) drawer.findViewById(R.id.userName));
                            if (userName != null && wizardUser != null && wizardUser.getUsername() != null) userName.setText(wizardUser.getUsername());
//                    userName.setText("wizardUser.getUsername()");
                            TextView tvemail = ((TextView) findViewById(R.id.userEmail));
                            if (tvemail != null) tvemail.setText(mAuth.getCurrentUser().getEmail());


                        }
                    }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception exception) {
                    // Handle any errors
                }
            });


        }
    }


    @Override
    protected void onResume() {
        super.onResume();
        getFireUser();
        loadUserDrawer();
        DrawerLayout drawer =  ( (DrawerLayout) findViewById(R.id.drawer_layout));
        drawer.openDrawer(GravityCompat.START);


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
//
//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.activity_main_drawer, menu);
//        menu.findItem(R.id.nav_add_mcq).setVisible(false);
//        return true;
//    }

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
            loadFragment(new OnlineFragment());
        } else if (id == R.id.nav_offline) {
            loadFragment(new OfflineFragment());
        } else if (id == R.id.nav_download) {
            loadFragment(new DownloadFragment());
        } else if (id == R.id.nav_manual) {
            loadFragment(new HelpInfoFragment());
        } else if (id == R.id.nav_leader_board) {
            loadFragment(new LeaderBoardFragment());
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


    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }


    private void userDataIsNull(DatabaseReference dbRefDefault) {
        WizardUser wizz = new WizardUser();
        FirebaseUser muser = mAuth.getCurrentUser();
        String name = muser.getEmail().split("@")[0];

        wizz.setUsername(name);
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


        ((TextView) findViewById(R.id.userName)).setText(wizz.getUsername());
        ((TextView) findViewById(R.id.userEmail)).setText(mAuth.getCurrentUser().getEmail());

        Bitmap bm = BitmapFactory.decodeResource(this.getResources(),
                R.drawable.android_plain_wordmark);
        uploadBitmapFirebase(bm);
        bm = getRoundedCornerBitmap(bm, 10);
        ((ImageView) findViewById(R.id.profileImageView)).setImageBitmap(bm);

    }


    public static Bitmap getRoundedCornerBitmap(Bitmap bitmap, int pixels) {
        Bitmap output = Bitmap.createBitmap(bitmap.getWidth(), bitmap
                .getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(output);

        final int color = 0xff424242;
        final Paint paint = new Paint();
        final Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
        final RectF rectF = new RectF(rect);
        final float roundPx = pixels;

        paint.setAntiAlias(true);
        canvas.drawARGB(0, 0, 0, 0);
        paint.setColor(color);
        canvas.drawRoundRect(rectF, roundPx, roundPx, paint);

        paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.SRC_IN));
        canvas.drawBitmap(bitmap, rect, rect, paint);

        return output;
    }

    private void uploadBitmapFirebase(Bitmap bitmap){

        if (bitmap != null) {
//            progressBar.setVisibility(View.VISIBLE);

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
//                    progressBar.setVisibility(View.GONE);
//                    profileImageUrl = profileImageRef.getDownloadUrl().toString();

                }
            });

        }
    }




//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        super.onCreateOptionsMenu(menu);
//        menu.findItem(R.id.nav_add_mcq).setVisible(true);
////                findViewById(R.id.nav_add_mcq)).setVisible(true);
//
//        return true;
//
//    }

    /**
     * Prepare the Screen's standard options menu to be displayed.  This is
     * called right before the menu is shown, every time it is shown.  You can
     * use this method to efficiently enable/disable items or otherwise
     * dynamically modify the contents.
     *
     * <p>The default implementation updates the system menu items based on the
     * activity's state.  Deriving classes should always call through to the
     * base class implementation.
     *
     * @param menu The options menu as last shown or first initialized by
     *             onCreateOptionsMenu().
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     * @see #onCreateOptionsMenu
     */
//    @Override
//    public boolean onPrepareOptionsMenu(Menu menu) {
//                super.onPrepareOptionsMenu(menu);
//        menu.findItem(R.id.nav_add_mcq).setVisible(true);
//
//        return true;
//
//    }
}
