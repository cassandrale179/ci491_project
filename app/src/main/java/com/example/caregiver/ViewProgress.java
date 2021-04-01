package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.TextView;

import com.example.caregiver.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ViewProgress extends AppCompatActivity {
    private String caregiveeName;
    private String caregiveeID;
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
        ImageView backArrow = findViewById(R.id.backArrowButton);
        backArrow.setOnClickListener(view -> {
            onBackPressed();
        });


        // Load all caregivee tasks
        Task taskModelObject = new Task();
        taskModelObject.getAllTasks(caregiveeID, new App.TaskCallback() {
            @Override
            public void onDataGot(List<Task> tasks){
                renderTaskList(tasks);
                renderTimeList(tasks);
            }
        });
    }

    /**
     * Render the list on the left to display tasks a caregivee have completed
     * @param taskList A list of task objects the caregivee have completed.
     */
    protected void renderTaskList(List<Task> taskList){
        final ListView taskListView =  findViewById(R.id.taskList);
        List <Map< String,  String >> data = new ArrayList < Map < String, String >> ();
        for (Task t: taskList) {
            Map< String, String > taskItem = new HashMap< String,
                    String >(2);
            taskItem.put("title", t.taskName);
            if (t.timeCompleted != -1){
                String sub = "Time completed: " + t.timeCompleted + " seconds";
                taskItem.put("subtitle", sub);
            } else {
                taskItem.put("subtitle", "Time completed: N/A");
            }
            data.add(taskItem);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this, data, android.R.layout.simple_list_item_2, new String[]{
                "title",
                "subtitle"
        },
                new int[]{
                        android.R.id.text1,
                        android.R.id.text2
                });
        taskListView.setAdapter(adapter);
    }

    /**
     * Render the list on the right to display status of completed task
     * @param taskList
     */
    protected  void renderTimeList(List<Task> taskList){
        final ListView timeListView =  findViewById(R.id.timeList);
        List <Map< String,  String >> data = new ArrayList < Map < String, String >> ();
        for (Task t: taskList) {
            Map< String, String > taskItem = new HashMap< String,
                    String >(2);
            taskItem.put("title", t.completionStatus);
            taskItem.put("subtitle", ""); // we need this to align two list
            data.add(taskItem);
        }

        SimpleAdapter adapter = new SimpleAdapter(
                this, data, android.R.layout.simple_list_item_2, new String[]{
                "title",
                "subtitle"
        },
                new int[]{
                        android.R.id.text1,
                        android.R.id.text2
                });
        timeListView.setAdapter(adapter);
    }
}