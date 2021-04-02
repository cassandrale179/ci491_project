package com.example.caregiver.model;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;

import com.example.caregiver.App;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;




/** This represents a single task object
 *  Is Parcelable in order to serialize Task object and pass to new intent
 *  */
public class Task implements Parcelable {
    public String caregiveeId; /* the caregivee associated with the task */
    public String caregiverId; /* the caregiver who assigned the task */
    public String taskId; /* the task id, auto-generated by Firebase */
    public String taskName; /* task actions (e.g Brush Your Teeth) */
    public String taskNote; /* task notes (Your Brush Is The Red One) */
    public boolean assignedStatus; /* true = task assigned to caregivee to do, false otherwise*/
    public String completionStatus; /* caregivee complete the tasks (complete), incomplete otherwise */
    public String room; /* room in which task was assigned */
    public int timeCompleted = -1; /* (optional) time take to complete a task in seconds */
    public long dateCompleted = -1; /* (optional) DATE when a task was completed in EpochTime */

    // Default constructor
    public Task() {};

    // Alternative constructor
    public Task(String caregiveeId, String caregiverId, String taskId, String taskName, String taskNote,
                boolean assignedStatus, String completionStatus, String roomStr) {
        this.caregiveeId = caregiveeId;
        this.caregiverId = caregiverId;
        this.taskId = taskId;
        this.taskName = taskName;
        this.taskNote = taskNote;
        this.completionStatus = completionStatus;
        this.assignedStatus = assignedStatus;
        this.room = roomStr;
    }

    // Alternative constructor for completed task
    public void setTimeCompleted(int timeCompleted) {
        this.timeCompleted = timeCompleted;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected Task(Parcel in) {
        caregiveeId = in.readString();
        caregiverId = in.readString();
        taskId = in.readString();
        taskName = in.readString();
        taskNote = in.readString();
        assignedStatus = in.readBoolean();
        completionStatus = in.readString();
        room = in.readString();
    }

    public static final Creator<Task> CREATOR = new Creator<Task>() {
        @RequiresApi(api = Build.VERSION_CODES.Q)
        @Override
        public Task createFromParcel(Parcel in) {
            return new Task(in);
        }

        @Override
        public Task[] newArray(int size) {
            return new Task[size];
        }
    };


    @Override
    public int describeContents() {
        return 0;
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(caregiveeId);
        dest.writeString(caregiverId);
        dest.writeString(taskId);
        dest.writeString(taskName);
        dest.writeString(taskNote);
        dest.writeBoolean(assignedStatus);
        dest.writeString(completionStatus);
        dest.writeString(room);
    }

    /**
     * Returns all tasks associated with that caregivee.
     * @param caregiveeId the String that represent the caregivee ID
     */
    public void getAllTasks(String caregiveeId, App.TaskCallback callback) {
        Gson gson = new Gson();

        // Initialize an array list that will store all tasks associated with the caregivee.
        List<Task> tasks = new ArrayList<>();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users/" + caregiveeId);
        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)@Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object firebaseRooms = snapshot.child("rooms").getValue();
                if (firebaseRooms != null) {
                    // Parse the roomString to return a json Object representation.
                    JsonParser parser = new JsonParser();
                    JsonObject roomObject = (JsonObject) parser.parse(gson.toJson(firebaseRooms));
                    List<String> rooms = roomObject.entrySet().stream().map(
                            i -> i.getKey()).collect(Collectors.toCollection(ArrayList::new));

                    // For each room, get their corresponding tasks
                    for (String roomStr : rooms) {
                        JsonObject singleRoom = roomObject.getAsJsonObject(roomStr);
                        JsonObject tasksPerRoom = singleRoom.getAsJsonObject("tasks");
                        if (tasksPerRoom != null) {
                            List<String> tasksIds = tasksPerRoom.entrySet().stream().map(
                                    i -> i.getKey()).collect(Collectors.toCollection(ArrayList::new));

                            // For each task, put them in the Task object.
                            for (String taskId : tasksIds) {

                                JsonObject task = tasksPerRoom.getAsJsonObject(taskId);
                                String caregiverId = task.get("caregiverID").getAsString();
                                String taskName = task.get("name").getAsString();
                                String taskNote = task.get("notes").getAsString();
                                Boolean assignedStatus = task.get("assignedStatus").getAsBoolean();
                                String completionStatus = task.get("completionStatus").getAsString();

                                Task t = new Task(caregiveeId, caregiverId, taskId, taskName, taskNote,
                                        assignedStatus, completionStatus, roomStr);

                                // If task is completed, set the time and date when task is completed.
                                if (completionStatus.equals("complete")){
                                    if (task.get("progress") != null){
                                        JsonObject progress = task.get("progress").getAsJsonObject();
                                        setCompletionDateAndTime(progress, t);
                                    }
                                }
                                tasks.add(t);
                            }
                        }
                    }
                }
                callback.onDataGot(tasks);
            }@Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("error", "Can't query caregivees for this caregiver");
            }
        });
    }

    /**
     * Set the time and date when a task is completed
     * @param progress the progress object that store all time and date when user complete tasks.
     * @param task the task object that contains the progress object.
     */
    protected static void setCompletionDateAndTime(JsonObject progress, Task task){
        int completionTime = -1;
        long completionDate = -1;

        // Since user complete a task MULTIPLE times, we want to get the last
        // date when the user complete a task.
        Set<Map.Entry<String, JsonElement>> entries = progress.entrySet();
        for (Map.Entry<String, JsonElement> entry: entries) {
            if (entry.getKey().matches("-?\\d+")){ // check if key is an integer
                long epochDate = Long.valueOf(entry.getKey());
                if (epochDate > completionDate){
                    completionDate = epochDate;
                    try{
                        completionTime = entry.getValue().getAsInt();
                    } catch (Exception e){
                        Log.d("error", "Invalid completion time");
                    }

                }
            }
        }
        task.dateCompleted = completionDate;
        task.timeCompleted = completionTime;
    }
}