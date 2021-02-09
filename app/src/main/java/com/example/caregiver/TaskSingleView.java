package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.text.Html;
import android.widget.TextView;

/**
 * This function is accessible to Caregivee only. It allows them to click on a Task and
 * start a timer to count down when the task is completed
 */
public class TaskSingleView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_single_view);

        // Programtically set background color for the buttons
        TextView playButton = findViewById(R.id.playButton);
        GradientDrawable playBg = (GradientDrawable)playButton.getBackground();
        playBg.setColor(getResources().getColor(R.color.teal_700));

        TextView timer = findViewById(R.id.timer);
        GradientDrawable timerBg = (GradientDrawable)timer.getBackground();
        timerBg.setColor(getResources().getColor(R.color.black));

        TextView helpBtn = findViewById(R.id.helpMe);
        GradientDrawable helpBg = (GradientDrawable)helpBtn.getBackground();
        helpBg.setColor(getResources().getColor(R.color.gray));
        helpBtn.setTextColor(Color.BLACK);

    }
}