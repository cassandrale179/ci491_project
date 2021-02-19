package com.example.caregiver;

import java.time.Duration;

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
