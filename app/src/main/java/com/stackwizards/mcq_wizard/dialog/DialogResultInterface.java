package com.stackwizards.mcq_wizard.dialog;

import android.app.Activity;
import android.view.View;

import com.stackwizards.mcq_wizard.entity.Question;

import java.util.ArrayList;

public interface DialogResultInterface {


    public Activity getmActivity();

    public View getView();

    public int getPointScore();

    public ArrayList<Question> getQuestion_array();
}
