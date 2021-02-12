package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.widget.TextView;

import org.w3c.dom.Text;

public class TaskFinish extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_finish);

        TextView timerCircle = findViewById(R.id.timerCircle);
        GradientDrawable helpBg = (GradientDrawable) timerCircle.getBackground();
        helpBg.setColor(getResources().getColor(R.color.teal_700));
    }
}