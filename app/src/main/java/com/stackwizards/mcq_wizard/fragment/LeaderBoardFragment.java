package com.stackwizards.mcq_wizard.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import com.stackwizards.mcq_wizard.R;

import com.stackwizards.mcq_wizard.adapter.CustomAdapter;

import com.stackwizards.mcq_wizard.adapter.ViewAdapterLeaderBoard;
import com.stackwizards.mcq_wizard.entity.WizardUser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class LeaderBoardFragment extends Fragment {
    //
    private View view;
    private Context context;
    private Activity mActivity;

    FirebaseAuth mAuth;
    DatabaseReference dbRef;
    WizardUser wizardUser;
    ListView simpleList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_leaderboard, container, false);
        this.mActivity = getActivity();
        context = mActivity.getApplicationContext();


        wizardUser = new WizardUser();
        mAuth = FirebaseAuth.getInstance();

        simpleList = (ListView) view.findViewById(R.id.simple_list_view);

        dbRef = FirebaseDatabase.getInstance().getReference().child("LeaderBoard");
        dbRef.addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String text = "";
//                Map<String,WizardUser> mcqLeader = new HashMap<>();
                ArrayList<ViewAdapterLeaderBoard> viewAdapterLeaderBoardArrayList = new ArrayList<>();
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    text += child.getKey() + "\n";
                    ViewAdapterLeaderBoard viewAdapterLeaderBoard = new ViewAdapterLeaderBoard();
                    viewAdapterLeaderBoard.setMcqName(child.getKey());

                    DatabaseReference myChild = dbRef.child(child.getKey());
                    myChild.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            String pts = dataSnapshot.child("pointScore").getValue(String.class);
                            String name = dataSnapshot.child("userName").getValue(String.class);
                            viewAdapterLeaderBoard.setScore(pts);
                            viewAdapterLeaderBoard.setUserName(name);

                            String uuid = dataSnapshot.child("UserId").getValue(String.class);


                            StorageReference mImageRef = FirebaseStorage.getInstance().getReference("profilepics/" + uuid + "/profile.jpg");

                            if (mImageRef != null) {
                                final long ONE_MEGABYTE = 1024 * 1024;
                                mImageRef.getBytes(ONE_MEGABYTE)
                                        .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                            @Override
                                            public void onSuccess(byte[] bytes) {
                                                Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                                                viewAdapterLeaderBoard.setUserPic(bm);
                                                viewAdapterLeaderBoardArrayList.add(viewAdapterLeaderBoard);

                                                CustomAdapter customAdapter = new CustomAdapter(context, viewAdapterLeaderBoardArrayList);
                                                simpleList.setAdapter(customAdapter);
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
                        public void onCancelled(@NonNull DatabaseError databaseError) {

                        }
                    });



//                   mcqLeader.put( child.getKey(), wizardUser);
                }


//                Toast.makeText(context, " GOING TO FETCH: " + text + " XX ", Toast.LENGTH_LONG).show();

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }




        });


        return view;
    }


//    DatabaseReference dbRefLeaderBoard = FirebaseDatabase.getInstance().getReference().child("LeaderBoard").child(mQuestionaire.getIconName().replace(".png",""));
//
//            dbRefLeaderBoard.addValueEventListener(new ValueEventListener() {
//        @Override
//        public void onDataChange(DataSnapshot dataSnapshot) {
//            String pts = dataSnapshot.child("pointScore").getValue(String.class);
//            if(pts == null ||pointScore > Integer.parseInt(pts)) {
////                                                               Toast.makeText(getActivity(), "THISXXXXX:" + pts, Toast.LENGTH_LONG).show();
//                Map<String, String> boardLeader = new HashMap<>();
//                boardLeader.put("pointScore", pointScore + "");
//                boardLeader.put("UserId", mAuth.getUid());
//
//                dbRefLeaderBoard.setValue(boardLeader);
//            }
//        }
//
//        @Override
//        public void onCancelled(@NonNull DatabaseError databaseError) {
//
//        }
//    });


}