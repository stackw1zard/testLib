package com.stackwizards.mcq_wizard.dialog;

import android.app.Activity;
import android.view.View;

import com.stackwizards.mcq_wizard.entity.Question;

import java.util.ArrayList;

/*
    Use as callback, to display result in the onlineFragment questionnaire challenge
 */
public interface DialogResultInterface {

     Activity getmActivity();

     View getView();

     int getPointScore();

     ArrayList<Question> getQuestion_array();
}
