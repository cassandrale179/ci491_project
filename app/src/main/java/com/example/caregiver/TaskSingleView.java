package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.SystemClock;
import android.text.Html;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This function is accessible to Caregivee only. It allows them to click on a Task and
 * start a timer to count down when the task is completed
 */
public class TaskSingleView extends AppCompatActivity {

    public long timeWhenStopped = 0;
    public boolean timeStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_single_view);

        // Set background color for the timer button
        Chronometer timer = (Chronometer) findViewById(R.id.timer);
        GradientDrawable timerBg = (GradientDrawable)timer.getBackground();
        timerBg.setColor(getResources().getColor(R.color.black));

        // Set background color for the help me button
        TextView helpBtn = findViewById(R.id.helpMe);
        GradientDrawable helpBg = (GradientDrawable)helpBtn.getBackground();
        helpBg.setColor(getResources().getColor(R.color.gray));
        helpBtn.setTextColor(Color.BLACK);

        // Add listener on the back arrow on the single task view screen
        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(TaskSingleView.this, Dashboard.class);
                startActivity(i);
            }
        });

        ImageView playButton = findViewById(R.id.playBtn);
        // Set a timer on click of playButton
        playButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (timeStarted == false){
                    timer.setBase(SystemClock.elapsedRealtime());
                    timer.start();
                    timeStarted = true;
                } else {
                    timer.stop();
                    timeStarted = false;
                }

            }
        });
    }
}