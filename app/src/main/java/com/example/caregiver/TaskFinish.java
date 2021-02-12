package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
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
        String room = task.get("room").toString().replace("\"", "");
        String taskId = task.get("taskId").toString().replace("\"", "");
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference taskRef = database
                .child("users").child(caregiveeId)
                .child("rooms").child(room)
                .child("tasks").child(taskId)
                .child("progress");


        long now = Instant.now().toEpochMilli();

        Map<String, Object> progressUpdates = new HashMap<>();
        progressUpdates.put(String.valueOf(now), time);

        taskRef.updateChildren(progressUpdates, new DatabaseReference.CompletionListener() {
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

        Bundle b = getIntent().getExtras();
        if (b != null) {
            String taskStr = b.getString("finishTask");
            String taskTime = b.getString("finishTime");
            doSomething(taskStr, taskTime);
        } else {
            Log.d("error", "Cannot get task.");
        }
    }
}