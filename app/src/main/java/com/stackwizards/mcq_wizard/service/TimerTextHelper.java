package com.stackwizards.mcq_wizard.service;

import android.os.Handler;
import android.view.MenuItem;
import android.widget.TextView;

public class TimerTextHelper implements Runnable {
    private final Handler handler = new Handler();
    private final TextView textView;
//    private final ClipData.Item item;
    private volatile long startTime;
    private volatile long elapsedTime;
    private long timeToLive = 60;



    public TimerTextHelper(TextView textView) {
        this.textView = textView;
    }

    @Override
    public void run() {
        timeToLive -= 1;
        long millis = System.currentTimeMillis() - startTime;
        int seconds = (int) (timeToLive - ((int) (millis / 1000)));
        int minutes = seconds / 60;
        seconds = seconds % 60;
//        textView.setText(String.format("%d:%02d", minutes, seconds));
        textView.setText(String.format("%d", timeToLive));
//        timerText.setTitle(String.valueOf(timeToLive));

        if (elapsedTime == -1) {
            handler.postDelayed(this, 1000);
        }
    }

    public void start() {
        this.startTime = System.currentTimeMillis();
        this.elapsedTime = -1;
        handler.post(this);
    }

    public void stop() {
        this.elapsedTime = System.currentTimeMillis() - startTime;
        handler.removeCallbacks(this);
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public long getTimeToLive() {
        return timeToLive;
    }

    public void bonusTime(){
        this.timeToLive = 10 + timeToLive;
    }
    public void deducTime(){
        this.timeToLive = this.timeToLive - 5;
    }
    public void resetTime(){
        this.timeToLive = 60;
    }
}
