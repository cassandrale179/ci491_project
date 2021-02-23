package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.time.Duration;
import java.util.ArrayList;

public class ViewProgress extends AppCompatActivity {

    private String caregiveeName;
    private String caregiveeID;
    private TaskAdapter listAdapter;
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);

        // get our fields from the extras
        caregiveeName = getIntent().getStringExtra("caregiveeName");
        caregiveeID = getIntent().getStringExtra("caregiveeID");

        // Set the text to use the correct caregivee name
        TextView nameText = findViewById(R.id.progressNameText);
        nameText.setText("Progress chart for " + caregiveeName);

        // Setup back button
        ImageButton backArrow = findViewById(R.id.backArrowButton);
        backArrow.setOnClickListener(view ->
        {
            onBackPressed();
        });

        // Setup list view
        ListView list = findViewById(R.id.taskProgressList);
        listAdapter = new TaskAdapter(this, android.R.layout.simple_list_item_1, new ArrayList<>());
        attachTaskListToDB();
        list.setAdapter(listAdapter);

    }

    private Task[] getRoomTasks(DataSnapshot roomSnapshot)
    {
        ArrayList<Task> tasks = new ArrayList<>();
        for (DataSnapshot task : roomSnapshot.child("tasks").getChildren())
        {
            String name = task.child("name").getValue().toString();

            // Get the most recent progress
            int progressTime = -1, progressDuration = -1;
            for (DataSnapshot progress : task.child("progress").getChildren())
            {
                int currTime = -1;
                int currDuration = -1;
                try {
                    currTime = Integer.parseInt(progress.getKey());
                    currDuration = Integer.parseInt((String)progress.getValue());
                }
                catch (NumberFormatException e) {
                    // The data in the database is malformed somehow.
                    // For now just leave currTime and currProgress as -1 and we'll display
                    // N/A on the frontend.
                    // FIXME: Do real error handling here
                }
                if (currTime > progressTime)
                {
                    progressTime = currTime;
                    progressDuration = currDuration;
                }
            }
            String room = roomSnapshot.getKey();
            TaskStatus status = task.child("completionStatus").getValue().equals("complete") ? TaskStatus.Completed : TaskStatus.Incomplete;
            Task t = new Task(name,
                    progressDuration == -1 ? null : Duration.ofSeconds(progressDuration),
                    status);
            tasks.add(t);
        }
        return tasks.toArray(new Task[tasks.size()]);
    }

    public void attachTaskListToDB()
    {
        final DatabaseReference tasks = database.child("/users/" + caregiveeID + "/rooms");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                listAdapter.clear();
                for (DataSnapshot roomSnapshot: snapshot.getChildren())
                {
                    Task[] tasks = getRoomTasks(roomSnapshot);
                    for (Task task : tasks)
                    {
                        listAdapter.add(task);
                    }
                }
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAIL", "getAllTasks:onCancelled", error.toException());
            }
        };
        tasks.addValueEventListener(valueEventListener);
    }
}