package com.stackwizards.mcq_wizard.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
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
import com.stackwizards.mcq_wizard.dialog.DialogResultInterface;
import com.stackwizards.mcq_wizard.dialog.ResultDialog;
import com.stackwizards.mcq_wizard.entity.Question;
import com.stackwizards.mcq_wizard.entity.Questionaire;
import com.stackwizards.mcq_wizard.entity.WizardUser;
import com.stackwizards.mcq_wizard.service.TimerTextHelper;

import org.json.JSONArray;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static android.content.Context.AUDIO_SERVICE;


public class OnlineFragment extends Fragment implements DialogResultInterface {

    private View view;
    private Context context;
    private int pointScore = 0;
    private Activity mActivity;


    FirebaseAuth mAuth;

    DatabaseReference dbRef;
    DatabaseReference dbRefWizard;
    WizardUser wizardUser;
    ViewGroup insertPointJsonFiles, offlineBaseLayout, insertPoint, offlineQuestionView, mcqHeaderDetails;

    private RequestQueue mQueue;
    private ImageView mNextQuestion;


    private ArrayList<Question> question_array;
    private List<Question> restQuestions;
    private int questionPointerNum = 0;

    boolean loaded = false;
    private float volume = 0;
    private Menu mMenu;

    TextView response, mQuestionText, mQuestionHint, mcqStatusInfo, mTimerText;


    private SoundPool soundPool;

    private int soundIdSuccess;
    private int soundIdFailure;
    private int soundIdGameover;
    private int soundIdStart;

    TimerTextHelper timerTextHelper;

    private Questionaire mQuestionaire = null;




    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_online, container, false);
// get the reference of Button
//        firstButton = (Button) view.findViewById(R.id.firstButton);
// perform setOnClickListener on first Button
//        firstButton.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//// display a message by using a Toast
//                Toast.makeText(getActivity(), "Fragment's Button", Toast.LENGTH_LONG).show();
//            }
//        });
        this.mActivity = getActivity();
        context = mActivity.getApplicationContext();

        offlineQuestionView = view.findViewById(R.id.offline_question_view);

        mTimerText = view.findViewById(R.id.mcq_timer_text);

        mcqStatusInfo = view.findViewById(R.id.mcq_info_details);


        FirebaseApp.initializeApp(getActivity());
//        FireBaseQuestionaires.this.setTitle("");

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mQueue = Volley.newRequestQueue(context);

        mAuth = FirebaseAuth.getInstance();

        question_array = new ArrayList<>();

        insertPointJsonFiles = view.findViewById(R.id.insert_point_json_files);
        mQuestionText = (TextView) view.findViewById(R.id.offline_question_text);
        mQuestionHint = (TextView) view.findViewById(R.id.offline_question_hint);
        insertPoint = view.findViewById(R.id.insert_point);
        mcqHeaderDetails = view.findViewById(R.id.mcq_header_details);

        mNextQuestion = view.findViewById(R.id.btn_next);
        mNextQuestion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                questionPointerNum = nextQuestion(question_array, questionPointerNum);
            }
        });


        dbRef = FirebaseDatabase.getInstance().getReference().child("Questionaires");
        dbRef.addValueEventListener(new ValueEventListener() {


            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                String text = "";
                //            str += (file[i].getName() + "\n");
                LayoutInflater inflater = getActivity().getLayoutInflater();

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    View mcqView = inflater.inflate(R.layout.mcq_view_adpater, null);
                    //Here you can access the child.getKey()
                    Questionaire questionaire = child.getValue(Questionaire.class);
                    text += (questionaire.getIconName() + " \n ");

//                    TextView mcqText = ((TextView) mcqView.findViewById(R.id.mcqTextView));
//                    mcqText.setText(questionaire.getIconName());
//                    String ic = (String) dbRef.child(child.getKey()).child("iconName").toString();
//                   Questionaire hallo =  (Questionaire) dbRef.child(child.getKey()).getClass();
//                    mcqText.setOnClickListener(new View.OnClickListener() {
//                        @Override
//                        public void onClick(View v) {
//                            ArrayList<Question> qs = jsonParse(questionaire.getJsonUrl(),questions);
//                            Toast.makeText(FireBaseQuestionaires.this, " GOING TO FETCH: " + questionaire.getJsonUrl() + "  " + qs.size() , Toast.LENGTH_LONG).show();
//
//                        }
//                    });

                    Log.e("TestQ", "QuestionaireId: " + child.getKey());

                    StorageReference mImageRef = FirebaseStorage.getInstance().getReference("questionaires/icons/" + questionaire.getIconName());

                    if (mImageRef != null) {
                        final long ONE_MEGABYTE = 1024 * 1024;
                        mImageRef.getBytes(ONE_MEGABYTE)
                                .addOnSuccessListener(new OnSuccessListener<byte[]>() {
                                    @Override
                                    public void onSuccess(byte[] bytes) {
                                        Bitmap bm = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
//                                        DisplayMetrics dm = new DisplayMetrics();
//                                        getWindowManager().getDefaultDisplay().getMetrics(dm);

                                        ImageView mcqImage = ((ImageView) mcqView.findViewById(R.id.mcqImageView));
                                        mcqImage.setImageBitmap(bm);
                                        mcqImage.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (questionaire.getUid() == null) {
                                                    questionaire.setUid(child.getKey());
                                                }
                                                mQuestionaire = questionaire;

                                                question_array = jsonParse(questionaire.getJsonUrl());
                                                Toast.makeText(context, " GOING TO FETCH: " + questionaire.getJsonUrl() + "  " + question_array.size(), Toast.LENGTH_LONG).show();
                                                insertPointJsonFiles.setVisibility(View.GONE);
                                                ((ViewGroup) view.findViewById(R.id.offline_question_view)).setVisibility(View.VISIBLE);
                                                ((ImageView) view.findViewById(R.id.btn_next)).setVisibility(View.VISIBLE);
                                            }
                                        });
//                                        imageView.setMinimumHeight(dm.heightPixels);
//                                        imageView.setMinimumWidth(dm.widthPixels);
//                                        imageView.setImageBitmap(bm);
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                // Handle any errors
                            }
                        });

                    }

                    insertPointJsonFiles.addView(mcqView, 0);


                }

                ((TextView) view.findViewById(R.id.fire_questionaires)).setText(text);
            }


            @Override
            public void onCancelled(DatabaseError databaseError) {
                System.out.println("The read failed: " + databaseError.getCode());
            }
        });


        dbRefWizard = FirebaseDatabase.getInstance().getReference().child("Members").child(mAuth.getUid());

        dbRefWizard.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                wizardUser = dataSnapshot.getValue(WizardUser.class);
                if (wizardUser.getQuestionaires() == null) {
                    ArrayList<Questionaire> qs = new ArrayList<>();
                    Questionaire test1 = new Questionaire();
                    test1.setUid("42");
                    test1.setIconName("clouds.png");
                    test1.setJsonUrl("http://www.stackwizards.org/json/test5.json");
                    qs.add(test1);
                    wizardUser.setQuestionaires(qs);
                    dbRefWizard.setValue(wizardUser);
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

//        dbRef = FirebaseDatabase.getInstance().getReference().child("Members");

//        findViewById(R.id.btnTestQuestion).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                String allQs = "";
//                for (int i = 0; i < question_array.size(); i++) {
//                    allQs += question_array.get(i).getQuestion_text();
//                }
//                ((TextView) findViewById(R.id.fire_questionaires)).setText(allQs);
//            }
//        });


        setAudioManager();
        timerTextHelper = new TimerTextHelper(mTimerText);
        timerTextHelper.start();

        return view;
    }


    private void setAudioManager() {


        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);

        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actualVolume / maxVolume;

//        // Set the hardware buttons to control the music
        getActivity().setVolumeControlStream(AudioManager.STREAM_MUSIC);
// Load the sound
        soundPool = new SoundPool(10, AudioManager.STREAM_MUSIC, 0);
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId,
                                       int status) {
                loaded = true;
            }
        });
        soundIdSuccess = soundPool.load(context, R.raw.success, 1);
        soundIdFailure = soundPool.load(context, R.raw.failure, 1);
        soundIdStart = soundPool.load(context, R.raw.sound_starter, 1);
        soundIdGameover = soundPool.load(context, R.raw.thezero__game_over_sound, 1);
    }


    private int nextQuestion(final ArrayList<Question> questions, int currentQuestionId) {
        mNextQuestion.setVisibility(View.GONE);
        mQuestionHint.setVisibility(View.GONE);
        mQuestionText.setVisibility(View.VISIBLE);
        Log.e("Test Wizard question", "Points: " + pointScore);

//        mMenu.findItem(R.id.rest_question).setTitle((currentQuestionId * 100) / questions.size() + "%  pts: " + pointScore);
        mcqStatusInfo.setText((currentQuestionId * 100) / questions.size() + "%  pts: " + pointScore);
        if (timerTextHelper.getTimeToLive() < 0 || currentQuestionId >= questions.size()) {
            currentQuestionId = -1;
            mQuestionText.setVisibility(View.GONE);
            mcqHeaderDetails.setVisibility(View.GONE);
            if (wizardUser != null) {

                ArrayList<Questionaire> questionaires = wizardUser.getQuestionaires();
                if (questionaires == null) {
                    Log.e("Test Wizard question", "Questionaire empty");

                    questionaires = new ArrayList<>();
                    questionaires.add(mQuestionaire);

                    wizardUser.setQuestionaires(questionaires);
//                    dbRefWizard.setValue(wizardUser);


                } else {
//                    List<Questionaire> qFilterList = questionaires.stream().filter(questionaire -> questionaire.getUid().equals(mQuestionaire.getUid()))
//                            .collect(Collectors.toList());
                    List<Questionaire> qFilterList = new ArrayList<>();
                    for (int qs = 0; qs < questionaires.size(); qs++) {
                        if (questionaires.get(qs).getUid().equals(mQuestionaire.getUid())) {
                            qFilterList.add(questionaires.get(qs));
                        }
                    }

                    Log.e("Test Wizard question", "Questionaire x empty");


                    if (questionaires == null || qFilterList.size() == 0) {
                        questionaires.add(mQuestionaire);
                        Log.e("Test Wizard question", "Questionaire NOT empty");

                    } else {
                        if (qFilterList != null && qFilterList.size() > 0) {
                            Questionaire questionImageIcon = qFilterList.get(0);
                            if (questionImageIcon.getScore() < pointScore) {
                                questionImageIcon.setScore(pointScore);
                                Log.e("Test Wizard question", "Questionaire setting score: " + pointScore);
                                pointScore = 0;

                            }
                        }

                    }

                    wizardUser.setQuestionaires(questionaires);
                }

//                questionairePointer =  questionaires.stream().
//                        filter(questionaire -> questionaire.getUid().equals(mQuestionaire.getUid()))
//                        .collect(Collectors.toList());
//
//
//                    questionaires.add(mQuestionaire);


            }


            insertPoint.setVisibility(View.GONE);
//            int result = (pointScore) / questions.size();
//
//            if (result < 0) {
//                result = 0;
//            } else if (result > 3) {
//                result = 3;
//            }
//
//            showCustomDialog(result);
//
//            Gson gson = new Gson();
//
//            if (limited) {
//                question_array.addAll(restQuestions);
//            }
//
//            Collections.sort(question_array,
//                    (m1, m2) -> (int) (m2.getNumOfTimesAsked() - m1.getNumOfTimesAsked()));
//            String jsonData = gson.toJson(question_array);
//
//            try {
//                myExternalFile = new File(getExternalFilesDir(filepath), selectedJsonFile);
//                FileOutputStream fos = new FileOutputStream(myExternalFile);
//                fos.write(jsonData.getBytes());
//                fos.close();
//            } catch (IOException e) {
//                Log.d("JSON PARSE DATA WRITTEN", e.getMessage());
//            }
//
//
            insertPointJsonFiles.setVisibility(View.VISIBLE);
            offlineQuestionView.setVisibility(View.GONE);
            Toast.makeText(getActivity(), "THIS WAS LAST QUESTION", Toast.LENGTH_LONG).show();


//            int result = (pointScore) / questions.size();
//
//            if (result < 0) {
//                result = 0;
//            } else if (result > 3) {
//                result = 3;
//            }

//            showCustomDialog(result);
            ResultDialog.showCustomDialog(this);

        } else {
            Questionaire questionaireHash;
            final Question question = questions.get(currentQuestionId);

            List<Questionaire> qFilterListUnfilter = wizardUser.getQuestionaires();
            List<Questionaire> qFilterList = new ArrayList<>();
            for (int qs = 0; qs < qFilterListUnfilter.size(); qs++) {
                if (qFilterListUnfilter.get(qs).getUid().equals(mQuestionaire.getUid())) {
                    qFilterList.add(qFilterListUnfilter.get(qs));
                }
            }

            if (qFilterList != null && qFilterList.size() > 0) {
                questionaireHash = qFilterList.get(0);
                if (pointScore > questionaireHash.getScore()) {
                    questionaireHash.setScore(pointScore);
                }

            }


            question.setNumOfTimesAsked(question.getNumOfTimesAsked() + 1);
            String qt = question.getQuestion_text();
            char ch = qt.substring(0, 1).charAt(0);
            Log.e("Test char", ch + "");

            while (Character.isDigit(ch)) {
                qt = qt.substring(1);
                ch = qt.substring(0, 1).charAt(0);

            }
            if (qt.startsWith(".") || qt.startsWith(")")) {
                qt = qt.substring(1);
            }

            mQuestionText.setText(qt);
            String answers = "";

            insertPoint.removeAllViews();
            Collections.shuffle(question.getAnswer_options());

            for (int aid = question.getAnswer_options().size() - 1; aid >= 0; aid--) {
                answers += ("\n" + question.getAnswer_options().get(aid) + "\n");
                LayoutInflater inflater = getActivity().getLayoutInflater();
                View questionView = inflater.inflate(R.layout.my_question, null);
                final Button ans = questionView.findViewById(R.id.textView);
                final int finalAid = aid;
                ans.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        questionPointerNum =  nextQuestion(questions, questionPointerNum, true);
                        getAnswer(questions, question.getAnswer_options().get(finalAid));
                    }
                });
                ans.setText(question.getAnswer_options().get(aid).substring(2));
                insertPoint.addView(questionView, 0);
            }

            insertPoint.setVisibility(View.VISIBLE);
        }
        DatabaseReference dbqREF = FirebaseDatabase.getInstance().getReference().child("Members").child(mAuth.getUid());
        dbqREF.setValue(wizardUser);
        return 1 + currentQuestionId;
    }


    private void getAnswer(ArrayList<Question> questions, String answer) {
        Question currentQuestion = questions.get(questionPointerNum - 1);

        mNextQuestion.setVisibility(View.VISIBLE);
        ArrayList<View> answerViews = insertPoint.getTouchables();
        for (int av = 0; av < answerViews.size(); av++) {
            Button btn = (Button) answerViews.get(av);

            if (!btn.getText().equals(currentQuestion.getAnswer().substring(2))) {
                btn.setText("");
            } else {
                // Is the sound loaded already?
                if (currentQuestion.getAnswer().equals(answer)) {
                    currentQuestion.setNumOfTimesAnsweredCorrectly(currentQuestion.getNumOfTimesAnsweredCorrectly() + 1);
                    if (loaded) {
                        pointScore += 4;
                        soundPool.play(soundIdSuccess, volume * 2, volume * 2, 1, 0, 1f);
                        Log.e("Test sound", "Played sound success");
                        btn.setBackgroundColor(0xFFA4C639);
                        timerTextHelper.bonusTime();
                    }
                } else {
                    if (loaded) {
                        pointScore -= 2;
                        soundPool.play(soundIdFailure, volume, volume, 1, 0, 1f);
                        Log.e("Test sound", "Played sound failure");
                        btn.setBackgroundColor(0xF0FFE666);
                        timerTextHelper.deducTime();
                    }
                }
            }
            btn.setOnClickListener(null);
        }


        if (currentQuestion.getHint() != null) {
            mQuestionHint.setText(currentQuestion.getHint());
            mQuestionHint.setVisibility(View.VISIBLE);
        }


    }


    private ArrayList<Question> jsonParse(String url) {
//        insertPoint.setVisibility(View.GONE);
        ArrayList<Question> questions = new ArrayList<>();

//        soundPool.play(soundIdStart, volume*3, volume*3, 1, 0, 1f);


//        timerTextHelper.resetTime();
//        mQuestionAnswer.setText("");
//        questions.clear();
//        questionPointerNum = 0;
//        pointScore = 0;
//        mMenu.findItem(R.id.timer_text).setVisible(false);
//        mMenu.findItem(R.id.rest_question).setVisible(false);

        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
//                        try {
                        Gson gson = new Gson();

                        Question[] qs = gson.fromJson(response.toString(), Question[].class);
                        questions.clear();
                        for (int qw = 0; qw < qs.length; qw++) {
                            Log.d("Counting", "counting questions: " + qs[qw].getAnswer());
                            Log.d("Counting", "answers questions: " + qs[qw].getAnswer_options().toString());

                            String ans = qs[qw].getAnswer().replace("Ans:", "").trim();
                            for (int cn = 0; cn < qs[qw].getAnswer_options().size(); cn++) {
                                if (ans.length() == 1 && qs[qw].getAnswer_options().get(cn).substring(0, 3).contains(ans)) {
                                    qs[qw].setAnswer(qs[qw].getAnswer_options().get(cn));
                                }
                            }


                            questions.add(qs[qw]);
                        }
                        Collections.shuffle(questions);

//                            questions.addAll(Arrays.asList(qs));

//                            for (int i = 0; i < response.length(); i++) {
//                                JSONObject question = response.getJSONObject(i);
//
//                                String questionText = question.getString("question_text");
////                                int age = employee.getInt("age");
////                                String mail = employee.getString("mail");
//
////                                mTextViewAnswers.append( response.length() + "\n\n");
////                                mQuestionAnswer.append("\n" + questionText + "\n");
//                                Question qi = new Question();
//                                String qt = question.getString("question_text");
//                                char ch = qt.substring(0, 1).charAt(0);
//                                while (Character.isDigit(ch)) {
//                                    qt = qt.substring(1);
//                                    ch = qt.substring(0, 1).charAt(0);
//
//                                }
//                                if (qt.startsWith(".")) {
//                                    qt = qt.substring(1);
//                                } else if (qt.startsWith(")")) {
//                                    qt = qt.substring(1);
//                                }
//                                qi.setQuestion_text(qt);
//                                String answer = "No answer was found ...";
//
//                                JSONArray questionOptions = new JSONArray(question.getString("answer_options"));
//                                ArrayList<String> answers = new ArrayList<>();
//                                for (int qa = 0; qa < questionOptions.length(); qa++) {
//                                    answers.add(questionOptions.getString(qa));
//                                    String tmpAnswer = question.getString("answer");
//                                    Log.d("ANSWER", "onResponse: ");
//                                    if (tmpAnswer.length() >= 6 && (questionOptions.getString(qa).startsWith(tmpAnswer.substring(5, 6)) ||
//                                            questionOptions.getString(qa).substring(0, 3).contains(tmpAnswer.substring(5, 6)))) {
//                                        answer = questionOptions.getString(qa);
//                                    }
//                                }
//                                qi.setAnswer_options(answers);
//
//                                qi.setAnswer(answer);
//
//                                questions.add(qi);
//                            }

//                            Collections.shuffle(questions);
//                            LinearLayout iconContainer = findViewById(R.id.icon_container);
//                            iconContainer.setVisibility(View.GONE);
//
//                            ScrollView questionView = findViewById(R.id.question_view);
//                            questionView.setVisibility(View.VISIBLE);
//                            mQuestionText.setText("Questions? Press right to start testing your know what..");
//                            insertPoint.setVisibility(View.GONE);
////                            mQuestionBtnAnswer.setVisibility(View.VISIBLE);
//                            mNextQuestion.setVisibility(View.VISIBLE);
//
//                            mMenu.findItem(R.id.timer_text).setVisible(true);
//                            mMenu.findItem(R.id.rest_question).setTitle("Time Ticking: ");
//                            mMenu.findItem(R.id.rest_question).setVisible(true);
//
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);

        timerTextHelper.resetTime();
        mcqHeaderDetails.setVisibility(View.VISIBLE);
        return questions;

    }




    public Activity getmActivity() {
        return mActivity;
    }

    public View getView() {
        return view;
    }

    public int getPointScore() {
        return pointScore;
    }

    public ArrayList<Question> getQuestion_array() {
        return question_array;
    }



    //    private void showCustomDialog(int msgIndex) {
//        //before inflating the custom alert dialog layout, we will get the current mActivity viewgroup
//        String[] results = {"Dude, you'll have to try harder", "Almost There, keep at it.", "A Real Wizard get more that 100%", "You are my Master ;)"};
//        ViewGroup viewGroup = view.findViewById(android.R.id.content);
//
//
//        //then we will inflate the custom alert dialog xml that we created
//        View dialogView = LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.my_dialog, viewGroup, false);
//
//
//        //Now we need an AlertDialog.Builder object
//        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
//
//        //setting the view of the builder to our custom view that we already inflated
//        builder.setView(dialogView);
//
//
//        //finally creating the alert dialog and displaying it
//        AlertDialog alertDialog = builder.create();
//
//        dialogView.findViewById(R.id.btnGameOver).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                alertDialog.dismiss();
////                startActivity(new Intent(NoConnectionActivity.this, StatusActivity.class));
////                ((AppCompatActivity)NoConnectionActivity.this).finish();
//
//            }
//        });
//
//        TextView gameOverTextMessage = ((TextView) dialogView.findViewById(R.id.gameOverTextMessage));
//
//        gameOverTextMessage.setText(results[msgIndex] + "\n    Score: " + pointScore);
//        switch (msgIndex) {
//            case 0:
//                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_dude_icon);
//                break;
//            case 1:
//                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.stack_app_logout_icon);
//                break;
//            case 2:
//                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_pirate_icon);
//                break;
//            case 3:
//                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_wizard_icon);
//                break;
//        }
//
//        alertDialog.show();
////
//    }
//

}