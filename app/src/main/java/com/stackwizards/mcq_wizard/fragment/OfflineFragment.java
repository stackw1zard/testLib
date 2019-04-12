package com.stackwizards.mcq_wizard.fragment;


import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.stackwizards.mcq_wizard.R;
import com.stackwizards.mcq_wizard.entity.Question;

import org.json.JSONArray;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static android.content.Context.AUDIO_SERVICE;


public class OfflineFragment extends Fragment implements  View.OnClickListener{



    private RequestQueue mQueue;

    EditText jsonFilename, jsonUrlResources;
    TextView response, mQuestionText, mQuestionHint, mcqStatusInfo;

    File myExternalDir;


    ViewGroup insertPointJsonFiles, offlineBaseLayout, insertPoint, offlineQuestionView;


    private String filename = "SampleFile.txt";
    private String filepath = "MyFileStorage";
    private String defaultPath = "MyFileStorage";
    File myExternalFile;
    String myData = "";
    String selectedJsonFile = "test.json";
    ArrayList<String> filepaths;

    private ImageView mNextQuestion;
    private ArrayList<Question> question_array;
    private List<Question> restQuestions;
    private int questionPointerNum = 0;
    private int pointScore = 0;

    boolean loaded = false;
    private float volume = 0;
//    private Menu mMenu;


    private SoundPool soundPool;

    private int soundIdSuccess;
    private int soundIdFailure;
    private int soundIdGameover;
    private int soundIdStart;

    Boolean limited = false;

    private Context context;
    private Activity activity;
    private View mView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
// Inflate the layout for this fragment
        this.mView = inflater.inflate(R.layout.fragment_no_connection, container, false);
        offlineQuestionView = mView.findViewById(R.id.offline_question_view);

        this.activity = getActivity();
        this.context = this.activity.getApplicationContext();






//        NoConnectionActivity.this.setTitle("");
//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);

        mQueue = Volley.newRequestQueue(context);

        insertPointJsonFiles = mView.findViewById(R.id.insert_point_json_files);
        offlineBaseLayout = mView.findViewById(R.id.no_connection_add_resource);
        insertPoint = mView.findViewById(R.id.insert_point);


        jsonFilename = (EditText) mView.findViewById(R.id.jsonFileName);
        jsonUrlResources = (EditText) mView.findViewById(R.id.jsonUrlResource);
        response = (TextView) mView.findViewById(R.id.response);

        mQuestionText = (TextView) mView.findViewById(R.id.offline_question_text);
        mQuestionHint = (TextView) mView.findViewById(R.id.offline_question_hint);
        mcqStatusInfo = (TextView) mView.findViewById(R.id.mcq_info_details);



        mView.findViewById(R.id.getExternalStorageResources).setOnClickListener(this);
        mView.findViewById(R.id.getExternalStorage).setOnClickListener(this);
        mView.findViewById(R.id.saveExternalStorage).setOnClickListener(this);

        mNextQuestion = mView.findViewById(R.id.btn_next);
        mNextQuestion.setOnClickListener(this);


        if (!isExternalStorageAvailable() || isExternalStorageReadOnly()) {
            mView.findViewById(R.id.saveExternalStorage).setEnabled(false);
        }
//        else {
////            myExternalFile = new File(getExternalFilesDir(filepath), filename);
//        }


        File path = context.getExternalFilesDir(filepath);
        File f = new File(String.valueOf(path));
        final File file[] = f.listFiles();


        addMCQListerners(file);

        addDirectoryListerners(file);

        setAudioManager();






        return mView;
    }







    private void setAudioManager() {


        AudioManager audioManager = (AudioManager) context.getSystemService(AUDIO_SERVICE);

        float actualVolume = (float) audioManager
                .getStreamVolume(AudioManager.STREAM_MUSIC);
        float maxVolume = (float) audioManager
                .getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        volume = actualVolume / maxVolume;

//        // Set the hardware buttons to control the music
        activity.setVolumeControlStream(AudioManager.STREAM_MUSIC);
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


    private void addMCQListerners(final File file[]) {
        for (int i = 0; i < file.length; i++) {
            Log.d("Files", "FileName:" + file[i].getName());
            if (!file[i].isDirectory()) {
                Log.d("Files", "File is NOT directory: " + file[i].getName());

//            str += (file[i].getName() + "\n");
                LayoutInflater inflater = activity.getLayoutInflater();
                View questionView = inflater.inflate(R.layout.my_question, null);
                final Button ans = questionView.findViewById(R.id.textView);
                final String fname = file[i].getName();
                ans.setText(fname);
                ans.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        myData = "";
                        pointScore = 0;
                        selectedJsonFile = fname;
//                        mMenu.findItem(R.id.rest_question).setTitle("0%  pts: 0");

                        try {
                            File tmpFile = new File(context.getExternalFilesDir(filepath), fname);
                            FileInputStream fis = new FileInputStream(tmpFile);
                            DataInputStream in = new DataInputStream(fis);
                            BufferedReader br =
                                    new BufferedReader(new InputStreamReader(in));
                            String strLine;
                            while ((strLine = br.readLine()) != null) {
                                myData = myData + strLine;
                            }

                            in.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                        Gson gson = new Gson();
                        Question[] questions = gson.fromJson(myData, Question[].class);
                        String qs = "";
                        restQuestions = new ArrayList<>();
                        if (question_array != null) {
                            question_array.clear();
                        } else {
                            question_array = new ArrayList<>();
                        }
                        int index = 1;
                        for (Question q : questions) {
                            if(q.getIndex() != null){
                                final String uuid = UUID.randomUUID().toString().replace("-", "");
                                q.setIndex(uuid);
                            }
                            qs += q.getQuestion_text() + "\n";


                            String answer = "No answer was found ...";
                            for (int qa = 0; qa < q.getAnswer_options().size(); qa++) {

//                                if (q.getAnswer().length() >= 6 && (q.getAnswer_options().get(qa).startsWith(q.getAnswer().substring(5, 6)) ||
//                                        q.getAnswer_options().get(qa).substring(0, 3).contains(q.getAnswer().substring(5, 6)))) {
//                                if (q.getAnswer().length() >= 6 && q.getAnswer_options().get(qa).substring(0, 4).contains(q.getAnswer().substring(5, 8).trim()) ) {

//                                String ans = q.getAnswer().substring(q.getAnswer().length()-1, q.getAnswer().length());
                                String ans = q.getAnswer().replace("Ans:", "").trim();
                                Log.d("answer check", "   " + q.getAnswer_options().get(qa).substring(0, 1) + " ##" + ans + "## " + q.getAnswer() + "  " + qa);
                                if (!q.getAnswer().equals(q.getAnswer_options().get(qa)) && q.getAnswer_options().get(qa).substring(0, 2).contains(ans)) {

                                    Log.d("answer check", "" + q.getAnswer_options().get(qa) + " #XXXXXX## " + ans);
                                    q.setAnswer(q.getAnswer_options().get(qa));

                                }
                            }

                            question_array.add(q);
                        }






                        if (question_array.size() > 20) {
                            limited = true;
//                        Collections.sort(question_array, Comparator.comparing((q1,q2)-> Integer.compare( q1.getNumOfTimesAsked(), q2.getNumOfTimesAsked())) );
                            Collections.sort(question_array,
                                    (m1, m2) -> (int) (m1.getNumOfTimesAnsweredCorrectly() - m2.getNumOfTimesAnsweredCorrectly()));
                            List<Question> ques = question_array.subList(0, 20);
                            restQuestions = question_array.subList(20, question_array.size());
                            ArrayList<Question> tmp = new ArrayList<>();
                            for(Question q : ques){
                                tmp.add(q);
                            }
//                            ques.stream().forEach(q -> tmp.add(q));
                            question_array = tmp;
                        } else {
                            limited = false;
                        }


                        Collections.shuffle(question_array);

                        insertPointJsonFiles.setVisibility(View.GONE);
                        offlineQuestionView.setVisibility(View.VISIBLE);
                        mNextQuestion.setVisibility(View.VISIBLE);
//                        mMenu.findItem(R.id.rest_question).setVisible(true);
                        mQuestionText.setVisibility(View.GONE);
                        insertPoint.setVisibility(View.GONE);
                        response.setText("Data retrieved from Internal Storage...");

                    }
                });


                insertPointJsonFiles.addView(questionView, 0);
            }
        }
    }


    private void addDirectoryListerners(final File file[]) {
        for (int i = 0; i < file.length; i++) {
            Log.d("Files", "FileName:" + file[i].getName());
            if (file[i].isDirectory()) {
                Log.d("Files", "File is directory: " + file[i].getName());
//            str += (file[i].getName() + "\n");
                LayoutInflater inflater = activity.getLayoutInflater();
                View questionView = inflater.inflate(R.layout.my_question, null);
                final Button mcqFolder = questionView.findViewById(R.id.textView);
                final String fname = file[i].getName();
                mcqFolder.setText(fname);
                mcqFolder.setBackgroundColor(0xF000FFFF);
                mcqFolder.setTextSize(24);
                mcqFolder.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                mcqFolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        filepath = defaultPath + "/" + fname;
                        File path = context.getExternalFilesDir(filepath);
                        File f = new File(String.valueOf(path));
                        final File dirFiles[] = f.listFiles();

                        insertPointJsonFiles.removeAllViews();

                        addMCQListerners(dirFiles);
//                        insertPointJsonFiles.setVisibility(View.GONE);
//                        findViewById(R.id.offline_question_view).setVisibility(View.VISIBLE);
//                        mNextQuestion.setVisibility(View.VISIBLE);
//                        mMenu.findItem(R.id.rest_question).setVisible(true);
//                        mQuestionText.setVisibility(View.GONE);
//                        insertPoint.setVisibility(View.GONE);
                    }
                });


                insertPointJsonFiles.addView(questionView, 0);
            }

        }

    }


    private void showCustomDialog(int msgIndex) {
        //before inflating the custom alert dialog layout, we will get the current activity viewgroup
        String[] results = {"Dude, you'll have to try harder", "Almost There, keep at it.", "A Real Wizard get more that 100%", "You are my Master ;)"};
        ViewGroup viewGroup = mView.findViewById(android.R.id.content);

        //then we will inflate the custom alert dialog xml that we created
        View dialogView = LayoutInflater.from(context).inflate(R.layout.my_dialog, viewGroup, false);


        //Now we need an AlertDialog.Builder object
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);

        //setting the view of the builder to our custom view that we already inflated
        builder.setView(dialogView);


        //finally creating the alert dialog and displaying it
        AlertDialog alertDialog = builder.create();

        dialogView.findViewById(R.id.btnGameOver).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                alertDialog.dismiss();
//                startActivity(new Intent(NoConnectionActivity.this, StatusActivity.class));
//                ((AppCompatActivity)NoConnectionActivity.this).finish();

            }
        });

        TextView gameOverTextMessage = ((TextView) dialogView.findViewById(R.id.gameOverTextMessage));

        gameOverTextMessage.setText(results[msgIndex] + "\n    Score: " + pointScore);
        switch (msgIndex){
            case 0:
                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_dude_icon);
                break;
            case 1:
                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.stack_app_logout_icon);
                break;
            case 2:
                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_pirate_icon);
                break;
            case 3:
                ((ImageView) dialogView.findViewById(R.id.gameResultIcon)).setImageResource(R.drawable.skull_wizard_icon);
                break;
        }

        alertDialog.show();
//
    }


    private int nextQuestion(final ArrayList<Question> questions, int currentQuestionId) {
        mNextQuestion.setVisibility(View.GONE);
        mQuestionHint.setVisibility(View.GONE);
        mQuestionText.setVisibility(View.VISIBLE);

//        mMenu.findItem(R.id.rest_question).setTitle((currentQuestionId * 100) / questions.size() + "%  pts: " + pointScore);
        mcqStatusInfo.setText((currentQuestionId * 100) / questions.size() + "%  pts: " + pointScore);

        if (currentQuestionId >= questions.size()) {
            currentQuestionId = -1;

            int result = (pointScore) / questions.size();

            if (result < 0) {
                result = 0;
            } else if (result > 3) {
                result = 3;
            }

            showCustomDialog(result);

            Gson gson = new Gson();

            if (limited) {
                question_array.addAll(restQuestions);
            }

            Collections.sort(question_array,
                    (m1, m2) -> (int) (m2.getNumOfTimesAsked() - m1.getNumOfTimesAsked()));
            String jsonData = gson.toJson(question_array);

            try {
                myExternalFile = new File(context.getExternalFilesDir(filepath), selectedJsonFile);
                FileOutputStream fos = new FileOutputStream(myExternalFile);
                fos.write(jsonData.getBytes());
                fos.close();
            } catch (IOException e) {
                Log.d("JSON PARSE DATA WRITTEN", e.getMessage());
            }


            insertPointJsonFiles.setVisibility(View.VISIBLE);
            offlineQuestionView.setVisibility(View.GONE);
            Toast.makeText(context, "THIS WAS LAST QUESTION", Toast.LENGTH_LONG).show();
        } else {

            final Question question = questions.get(currentQuestionId);
            question.setNumOfTimesAsked(question.getNumOfTimesAsked() + 1);
            mQuestionText.setText(question.getQuestion_text());
            String answers = "";

            insertPoint.removeAllViews();
            for (int aid = question.getAnswer_options().size() - 1; aid >= 0; aid--) {
                answers += ("\n" + question.getAnswer_options().get(aid) + "\n");
                LayoutInflater inflater = activity.getLayoutInflater();
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
                ans.setText(question.getAnswer_options().get(aid));
                insertPoint.addView(questionView, 0);
            }

            insertPoint.setVisibility(View.VISIBLE);
        }
        return 1 + currentQuestionId;
    }


    private void getAnswer(ArrayList<Question> questions, String answer) {
        Question currentQuestion = questions.get(questionPointerNum - 1);

        mNextQuestion.setVisibility(View.VISIBLE);
        ArrayList<View> answerViews = insertPoint.getTouchables();
        for(View va : answerViews){
            Button btn = (Button) va;
            if (!btn.getText().equals(currentQuestion.getAnswer())) {
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
                    }
                } else {
                    if (loaded) {
                        pointScore -= 2;
                        soundPool.play(soundIdFailure, volume, volume, 1, 0, 1f);
                        Log.e("Test sound", "Played sound failure");
                        btn.setBackgroundColor(0xF0FFE666);

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



    private void saveMcqResourcesInFile(String url) {
        JsonArrayRequest request = new JsonArrayRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONArray>() {
                    @Override
                    public void onResponse(JSONArray response) {
                        try {
                            myExternalFile = new File(context.getExternalFilesDir(filepath), jsonFilename.getText().toString() + ".json");
                            FileOutputStream fos = new FileOutputStream(myExternalFile);
                            fos.write(response.toString().getBytes());
                            fos.close();
                        } catch (IOException e) {
                            Log.d("JSON PARSE", e.getMessage());
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
            }
        });

        mQueue.add(request);
    }




    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.getExternalStorageResources:
                offlineBaseLayout.setVisibility(View.GONE);
                insertPointJsonFiles.setVisibility(View.VISIBLE);
                break;
            case R.id.saveExternalStorage:
                saveMcqResourcesInFile(jsonUrlResources.getText().toString());
                break;
            case R.id.getExternalStorage:
                try {
                    FileInputStream fis = new FileInputStream(myExternalFile);
                    DataInputStream in = new DataInputStream(fis);
                    BufferedReader br =
                            new BufferedReader(new InputStreamReader(in));
                    String strLine;
                    myData = "";
                    while ((strLine = br.readLine()) != null) {
                        myData = myData + strLine;
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                jsonFilename.setText(myData);
                response.setText("SampleFile.txt data retrieved from Internal Storage...");
                break;
            case R.id.btn_next:
                questionPointerNum = nextQuestion(question_array, questionPointerNum);
                break;
        }
    }




}