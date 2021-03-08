package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.w3c.dom.Text;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TaskFinish extends AppCompatActivity {

    @RequiresApi(api = Build.VERSION_CODES.O)
    protected void doSomething(@NonNull String taskStr, @NonNull String time){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String caregiveeId = preferences.getString("userId", "");
        if (caregiveeId.isEmpty()) {
            return;
        }
        JsonObject task = new Gson().fromJson(taskStr, JsonObject.class);
        String room = task.get("room").getAsString();
        String taskId = task.get("taskId").getAsString();

        // Set task name on view
        String taskName = task.get("taskName").getAsString();
        TextView taskTitleView = findViewById(R.id.taskFinishTitle);
        taskTitleView.setText(taskName);

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference taskRef = database
                .child("users").child(caregiveeId)
                .child("rooms").child(room)
                .child("tasks").child(taskId);

        // Mark the task as completed
        taskRef.child("completionStatus").setValue("complete");
        taskRef.child("assignedStatus").setValue(false);

        // Get the date today in Epoch format
        long now = Instant.now().toEpochMilli();

        // Store the progress update as a key-value pair in the database
        Map<String, Object> progressUpdates = new HashMap<>();
        progressUpdates.put(String.valueOf(now), time);
        taskRef.child("progress").updateChildren(progressUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    Log.d("error", "can't upload user progress");
                } else {
                    Log.d("okay", "can upload.");
                }
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_finish);

        TextView timerCircle = findViewById(R.id.timerCircle);
        GradientDrawable helpBg = (GradientDrawable) timerCircle.getBackground();
        helpBg.setColor(getResources().getColor(R.color.teal_700));

        // Add listener on the back arrow on the single task view screen
        ImageView backArrow = (ImageView) findViewById(R.id.backArrow);
        backArrow.setOnClickListener(new View.OnClickListener() {@Override
        public void onClick(View v) {
            Intent i = new Intent(TaskFinish.this, Dashboard.class);
            startActivity(i);
        }
        });

        Bundle b = getIntent().getExtras();
        if (b != null) {
            String taskStr = b.getString("finishTask");
            String taskTime = b.getString("finishTime");
            timerCircle.setText(taskTime + ":00");
            doSomething(taskStr, taskTime);
        } else {
            Log.d("error", "Cannot get task.");
        }
    }
}