package com.stackwizards.mcq_wizard.entity;

import java.util.ArrayList;

/*  This class is used to serialised the questionaires to be stored and retrieved from firebase.
    The json files containing the question were stored on a different web server.
    This class will only contain the url to the json file.
 */
public class Questionaire {

    private String uid;
    private String iconName;
    private String jsonUrl;
    private ArrayList<QuestionStats> questionStats;

    private int score;

    public String getIconName() {
        return iconName;
    }

    public void setIconName(String iconName) {
        this.iconName = iconName;
    }

    public String getJsonUrl() {
        return jsonUrl;
    }

    public void setJsonUrl(String jsonUrl) {
        this.jsonUrl = jsonUrl;
    }

    public ArrayList<QuestionStats> getQuestionStats() {
        return questionStats;
    }

    public void setQuestionStats(ArrayList<QuestionStats> questionStats) {
        this.questionStats = questionStats;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public  class QuestionStats{
        String uuid;
        int asked;
        int answeredCorrectly;

        public String getUuid() {
            return uuid;
        }

        public void setUuid(String uuid) {
            this.uuid = uuid;
        }

        public int getAsked() {
            return asked;
        }

        public void setAsked(int asked) {
            this.asked = asked;
        }

        public int getAnsweredCorrectly() {
            return answeredCorrectly;
        }

        public void setAnsweredCorrectly(int answeredCorrectly) {
            this.answeredCorrectly = answeredCorrectly;
        }


    }

}
