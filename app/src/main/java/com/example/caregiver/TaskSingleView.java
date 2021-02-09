package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * This function is accessible to Caregivee only. It allows them to click on a Task and
 * start a timer to count down when the task is completed
 */
public class TaskSingleView extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_single_view);


    }
}