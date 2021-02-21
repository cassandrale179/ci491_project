package com.example.caregiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;
import android.view.View;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

/**
 * This function is accessible to Caregivee only.
 * Allows them to click on a Task and start a timer to measure time to do task.
 */
public class TaskSingleView extends AppCompatActivity {

    public long timeWhenStopped = 0;
    public boolean timeStarted = false;
    String taskStr = "";

    /** Function to set text and notes for a task */
    protected void setTitleAndNotes(){
        Bundle b = getIntent().getExtras();
        taskStr = b.getString("taskObject");
        TextView taskTitleField = findViewById(R.id.taskName);
        TextView taskNoteField = findViewById(R.id.taskNotes);

        // No task object to be found
        if (taskStr.isEmpty()){
            Log.d("error", "Can't find task title and notes");
            return;
        }

        JsonObject task = new Gson().fromJson(taskStr, JsonObject.class);

        // Task object lack important fields
        if (task.get("taskName") == null || task.get("taskNote") == null){
            return;
        }

        // Set task message and title
        String taskTitle = task.get("taskName").getAsString();
        String taskNotes = task.get("taskNote").getAsString();
        taskTitleField.setText(taskTitle);
        taskNoteField.setText(taskNotes);
    }

    /**
     * Handle timer counting and pause.
     * @param timer The timer button.
     */
    protected void setTimer(Chronometer timer){
        ImageView playButton = findViewById(R.id.playBtn);
        playButton.setOnClickListener(new View.OnClickListener() {@Override
        public void onClick(View v) {

            // If the timer hasn't start yet, start timer.
            if (timeStarted == false) {
                timer.setBase(SystemClock.elapsedRealtime() + timeWhenStopped);
                timer.start();
                timeStarted = true;
            }

            // When timer is paused, check if caregivee finished task.
            else {
                timer.stop();
                timeStarted = false;
                timeWhenStopped = timer.getBase() -  SystemClock.elapsedRealtime();
                AlertDialog.Builder builder = new AlertDialog.Builder(TaskSingleView.this);

                // Pop up open dialog-box to check if they actually finished task.
                builder.setMessage("Finish your task?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {@Override
                public void onClick(DialogInterface dialog, int which) {
                    Intent i = new Intent(TaskSingleView.this, TaskFinish.class);
                    long elapsedMillis = (SystemClock.elapsedRealtime() - timer.getBase()) / 1000;
                    i.putExtra("finishTime", String.valueOf(elapsedMillis));
                    i.putExtra("finishTask", taskStr);
                    startActivity(i);
                }
                }).setNegativeButton("No", null);
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
        });
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_single_view);

        // Set background color for the timer button
        Chronometer timer = (Chronometer) findViewById(R.id.timer);
        GradientDrawable timerBg = (GradientDrawable) timer.getBackground();
        timerBg.setColor(getResources().getColor(R.color.black));

        // Set background color for the help me button
        TextView helpBtn = findViewById(R.id.helpMe);
        GradientDrawable helpBg = (GradientDrawable) helpBtn.getBackground();
        helpBg.setColor(getResources().getColor(R.color.gray));
        helpBtn.setTextColor(Color.BLACK);

        // Add listener on the back arrow on the single task view screen
        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {@Override
        public void onClick(View v) {
            Intent i = new Intent(TaskSingleView.this, Dashboard.class);
            startActivity(i);
        }
        });

        setTitleAndNotes();
        setTimer(timer);
    }
}