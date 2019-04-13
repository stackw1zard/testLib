package com.stackwizards.mcq_wizard.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.toolbox.Volley;
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
import com.google.gson.Gson;
import com.stackwizards.mcq_wizard.R;

import com.stackwizards.mcq_wizard.adapter.CustomAdapter;
import com.stackwizards.mcq_wizard.entity.Question;
import com.stackwizards.mcq_wizard.entity.Questionaire;
import com.stackwizards.mcq_wizard.entity.WizardUser;

import java.io.File;
import java.util.ArrayList;

//TODO CHECK READ WRITE PERMISSION
public class LeaderBoardFragment extends Fragment {
    //
    private View view;
    private Context context;
    private Activity mActivity;

    private String filepath = "MyFileStorage";

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    ViewGroup insertPointJsonFiles;

    private RequestQueue mQueue;

    boolean loaded = false;
    WizardUser wizardUser;

    private Questionaire mQuestionaire = null;
    ArrayList<String> filenames;
    ListView simpleList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        this.mActivity = getActivity();
        context = mActivity.getApplicationContext();


        simpleList = (ListView) view.findViewById(R.id.simple_list_view);


        wizardUser = new WizardUser();
        mAuth = FirebaseAuth.getInstance();
        dbRef = FirebaseDatabase.getInstance().getReference().child("Members").child(mAuth.getUid());

        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wizardUser = dataSnapshot.getValue(WizardUser.class);
                String bio = (String) dataSnapshot.child("bio").getValue().toString();
//                wizardUser.setBio("xxx " + bio);
                String user = (String) dataSnapshot.child("username").getValue().toString();

//                wizardUser.setUsername("xxx " + user);
//                insertUserData();
//        CustomAdapter customAdapter = new CustomAdapter(getApplicationContext(), countryList, flags);
//               Toast.makeText(ProfileActivity.this,bio, Toast.LENGTH_SHORT).show();
                CustomAdapter customAdapter = new CustomAdapter(context, wizardUser);
                simpleList.setAdapter(customAdapter);
            }



            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });



//
//        FirebaseApp.initializeApp(getActivity());
////        FireBaseQuestionaires.this.setTitle("");
////        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
//        File path = context.getExternalFilesDir(filepath);
//        File f = new File(String.valueOf(path));
//        final File file[] = f.listFiles();
//        filenames = new ArrayList<>();
//        for (int i = 0; i < file.length; i++) {
//            Log.d("Files", "FileName:" + file[i].getName());
//            if (!file[i].isDirectory()) {
//                Log.d("Files", "File is NOT directory: " + file[i].getName());
//                filenames.add(file[i].getName());
//            }
//        }
//
//
//
//        mQueue = Volley.newRequestQueue(context);
//
//        mAuth = FirebaseAuth.getInstance();
//
//        insertPointJsonFiles = view.findViewById(R.id.insert_point_json_files);
//        dbRef = FirebaseDatabase.getInstance().getReference().child("Questionaires");
//        dbRef.addValueEventListener(new ValueEventListener() {
//
//
//            @Override
//            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                String text = "";
//                LayoutInflater inflater = getActivity().getLayoutInflater();
//
//                for (DataSnapshot child : dataSnapshot.getChildren()) {
//                    View mcqView = inflater.inflate(R.layout.mcq_view_adpater, null);
//                    //Here you can access the child.getKey()
//                    Questionaire questionaire = child.getValue(Questionaire.class);
//                    text += (questionaire.getIconName() + " \n ");
//
//
//                    Log.e("TestQ", "QuestionaireId: " + child.getKey());
//
//                    StorageReference mImageRef = FirebaseStorage.getInstance().getReference("questionaires/icons/" + questionaire.getIconName());
//
//                    if (mImageRef != null) {
//                        final long ONE_MEGABYTE = 1024 * 1024;
//                        mImageRef.getBytes(ONE_MEGABYTE)
//                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
//                                    @Override
//                                    public void onSuccess(byte[] bytes) {
//                                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//
//                                        ImageView mcqImage = ((ImageView) mcqView.findViewById(R.id.mcqImageView));
//                                        mcqImage.setImageBitmap(bm);
//                                        String tmpFileName =  questionaire.getJsonUrl();
//                                        tmpFileName = tmpFileName.substring(tmpFileName.lastIndexOf('/') + 1);
//                                        if(filenames.contains(tmpFileName)){
//                                            mcqImage.setBackgroundColor(0xFFA4C639);
//                                        }
//
//                                        mcqImage.setOnClickListener(new View.OnClickListener() {
//                                            @Override
//                                            public void onClick(View v) {
//                                                if (questionaire.getUid() == null) {
//                                                    questionaire.setUid(child.getKey());
//                                                }
//                                                mQuestionaire = questionaire;
//                                                mcqView.setVisibility(View.GONE);
//                                            }
//                                        });
//                                    }
//                                }).addOnFailureListener(new OnFailureListener() {
//                            @Override
//                            public void onFailure(@NonNull Exception exception) {
//                                // Handle any errors
//                            }
//                        });
//                    }
//                    insertPointJsonFiles.addView(mcqView, 0);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                System.out.println("The read failed: " + databaseError.getCode());
//            }
//        });
//

        return view;
        }


    }