package com.stackwizards.mcq_wizard.entity;

import java.util.ArrayList;

public class Question {
    String question_text;
    String answer;
    String hint;
    int numOfTimesAsked = 0;
    int numOfTimesAnsweredCorrectly = 0;
    ArrayList<String> answer_options;

    public String getQuestion_text() {
        return question_text;
    }

    public void setQuestion_text(String question_text) {
        this.question_text = question_text;
    }

    public String getAnswer() {
        return answer;
    }

    public void setAnswer(String answer) {
        this.answer = answer;
    }

    public String getHint() {
        return hint;
    }

    public void setHint(String hint) {
        this.hint = hint;
    }


    public int getNumOfTimesAsked() {
        return numOfTimesAsked;
    }

    public void setNumOfTimesAsked(int numOfTimesAsked) {
        this.numOfTimesAsked = numOfTimesAsked;
    }

    public int getNumOfTimesAnsweredCorrectly() {
        return numOfTimesAnsweredCorrectly;
    }

    public void setNumOfTimesAnsweredCorrectly(int numOfTimesAnsweredCorrectly) {
        this.numOfTimesAnsweredCorrectly = numOfTimesAnsweredCorrectly;
    }

    public ArrayList<String> getAnswer_options() {
        return answer_options;
    }

    public void setAnswer_options(ArrayList<String> answer_options) {
        this.answer_options = answer_options;
    }
}
