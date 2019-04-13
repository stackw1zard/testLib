package com.stackwizards.mcq_wizard.adapter;

import android.graphics.Bitmap;

public class ViewAdapterLeaderBoard {

    String mcqName;
    String userName;
    String score;
    Bitmap userPic;

    public String getMcqName() {
        return mcqName;
    }

    public void setMcqName(String mcqName) {
        this.mcqName = mcqName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getScore() {
        return score;
    }

    public void setScore(String score) {
        this.score = score;
    }

    public Bitmap getUserPic() {
        return userPic;
    }

    public void setUserPic(Bitmap userPic) {
        this.userPic = userPic;
    }
}
