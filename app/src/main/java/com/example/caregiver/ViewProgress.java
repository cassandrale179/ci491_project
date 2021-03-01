package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ListView;

import java.time.Duration;

public class ViewProgress extends AppCompatActivity {

    enum TaskStatus {Completed, Incomplete, InProgress};

    public class Task {

        // TODO: Eventually this will hold data from Firebase representing a task. For now this is a
        // dummy class so I can build a ListViewAdapter around it.

        private String name;
        private Duration timeCompleted;
        private TaskStatus status;

        public Task(String name, Duration timeCompleted, TaskStatus status)
        {
            this.name = name;
            this.timeCompleted = timeCompleted;
            this.status = status;
        }

        public String getName()
        {
            return this.name;
        }

        public Duration getTimeCompleted()
        {
            return this.timeCompleted;
        }

        public TaskStatus getStatus()
        {
            return this.status;
        }

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);

        // Setup back button
        ImageButton backArrow = findViewById(R.id.backArrowButton);
        backArrow.setOnClickListener(view ->
        {
            onBackPressed();
        });

        // Setup list view
        ListView list = findViewById(R.id.taskProgressList);
        Task[] taskArray = getTaskArrayFromDB();
        list.setAdapter(new TaskAdapter(this, android.R.layout.simple_list_item_1, taskArray));

    }

    public Task[] getTaskArrayFromDB()
    {
        // TODO: Eventually will query the DB for a list of tasks. For now, generates a dummy list.
        Task[] arr = {
                new Task("Brush Your Teeth", Duration.ofSeconds(200), TaskStatus.Completed),
                new Task("Wash Your Hands", Duration.ofSeconds(20), TaskStatus.Completed),
                new Task("Do Laundry", null, TaskStatus.Incomplete),
                new Task("Turn Off Light", Duration.ofSeconds(20), TaskStatus.Completed)
        };

        return arr;
    }
}