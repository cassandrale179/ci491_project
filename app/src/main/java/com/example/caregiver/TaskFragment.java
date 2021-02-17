package com.example.caregiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.google.gson.annotations.SerializedName;
import org.json. * ;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {

    /** This represent a single task object */
    public static class Task implements Parcelable{
        String caregiveeId; /* the caregivee associated with the task */
        String caregiverId; /* the caregiver who assigned the task */
        String taskId; /* the task id, auto-generated by Firebase */
        String taskName; /* task actions (e.g Brush Your Teeth) */
        String taskNote; /* task notes (Your Brush Is The Red One) */
        String assignedStatus; /* true = task assigned to caregivee to do, false otherwise*/
        String completionStatus; /* caregivee complete the tasks (complete), incomplete otherwise */
        String room; /* room in which task was assigned */

        // Default constructor
        public Task() {};

        // Alternative constructor to instantiate the task object.
        public Task(String caregiveeId, String caregiverId, String taskId, String taskName, String taskNote,
                    String assignedStatus, String completionStatus, String room) {
            this.caregiveeId = caregiveeId;
            this.caregiverId = caregiverId;
            this.taskId = taskId;
            this.taskName = taskName;
            this.taskNote = taskNote;
            this.completionStatus = completionStatus;
            this.assignedStatus = assignedStatus;
            this.room = room;
        }

        protected Task(Parcel in) {
            caregiveeId = in.readString();
            caregiverId = in.readString();
            taskId = in.readString();
            taskName = in.readString();
            taskNote = in.readString();
            assignedStatus = in.readString();
            completionStatus = in.readString();
            room = in.readString();
        }

        public static final Creator<Task> CREATOR = new Creator<Task>() {
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

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(caregiveeId);
            dest.writeString(caregiverId);
            dest.writeString(taskId);
            dest.writeString(taskName);
            dest.writeString(taskNote);
            dest.writeString(assignedStatus);
            dest.writeString(completionStatus);
            dest.writeString(room);
        }
    }

    // Global object representation of the view.
    View view;

    // This stores the R.android.id of the expandable list on Tasks page
    ExpandableListView caregiveeList;

    // Global reference to Firebase
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // This hashmap store the caregivee id (key) and their tasks (value)
    HashMap< String, List < Task >> taskList = new HashMap<>();

    // This hashmap store the caregivee id (key) and their name (value)
    HashMap< String, String > caregiveeInfo = new HashMap<>();

    // This hashmap store the caregivee id (key) and their rooms (value)
    HashMap < String, List< String >> caregiveeRooms = new HashMap<>();

    // This is the adapter for the
    MainAdapter adapter;

    // Default public constructor
    public TaskFragment() {}

    // Static instance initiator
    public static TaskFragment newInstance(String param1, String param2) {
        TaskFragment fragment = new TaskFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Return list of caregivees associated with the caregiver.
     */
    public void queryCaregivees() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = preferences.getString("userId", "");
        DatabaseReference ref = database.child("users/" + userId);

        ref.addValueEventListener(new ValueEventListener() {@Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot caregivee :  snapshot.child("caregivees").getChildren()) {
                String caregiveeId = caregivee.getKey();
                long size = snapshot.child("caregivees").getChildrenCount();
                getCaregiveeNameAndTask(caregiveeId, size);
            }

        }@Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("error", "Can't query caregivees for this caregiver");
        }
        });
    }

    /**
     * Query data for each caregivee.
     * @param caregiveeId the id of the caregivee
     * @param size the size of the caregivees list
     */
    protected void getCaregiveeNameAndTask(String caregiveeId, long size) {
        DatabaseReference ref = database.child("users/" + caregiveeId);
        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String name = dataSnapshot.child("name").getValue().toString();
            Object taskObject = dataSnapshot.child("rooms").getValue();
            if (taskObject != null) {
                Gson gson = new Gson();
                String tasksJson = gson.toJson(taskObject);
                List<Task> tasks = createRoomAndTaskObject(caregiveeId, tasksJson);
                taskList.put(caregiveeId, tasks);
            }
            caregiveeInfo.put(caregiveeId, name);

            // TODO: hacky way of display the list. Need to use async.
            if (caregiveeInfo.size() == size){
                displayCaregivee();
            }
        }@Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("failure", "Unable to obtain data for this caregivee " + caregiveeId);
        }
        });
    }

    /**
     * Returns all tasks associated with that caregivee.
     * @param caregiveeId The caregivee user id.
     * @param roomString Json-string representation of all the rooms.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    protected List<Task> createRoomAndTaskObject(String caregiveeId, String roomString) {

        // Initialize an array list that will store all tasks associated with the caregivee.
        List<Task> tasks = new ArrayList<>();

        // Parse the roomString to return a json Object representation.
        JsonParser parser = new JsonParser();
        JsonObject roomObject = (JsonObject) parser.parse(roomString);
        List < String > rooms = roomObject.entrySet().stream().map(i
                ->i.getKey()).collect(Collectors.toCollection(ArrayList::new));

        // Store the caregivee and their rooms
        caregiveeRooms.put(caregiveeId, rooms);

        // For each room, get their corresponding tasks
        for (String roomStr: rooms) {
            JsonObject singleRoom = roomObject.getAsJsonObject(roomStr);
            JsonObject tasksPerRoom = singleRoom.getAsJsonObject("tasks");
            if (tasksPerRoom != null) {
                List < String > tasksIds = tasksPerRoom.entrySet().stream().map(i
                        ->i.getKey()).collect(Collectors.toCollection(ArrayList::new));

                // For each task, put them in the Task object.
                for (String taskId: tasksIds) {
                    JsonObject task = tasksPerRoom.getAsJsonObject(taskId);
                    String caregiverId = task.get("caregiverID").getAsString();
                    String taskName = task.get("name").getAsString();
                    String taskNote = task.get("notes").getAsString();
                    String assignedStatus = task.get("assignedStatus").getAsString();
                    String completionStatus = task.get("completionStatus").getAsString();
                    Task t = new Task(caregiveeId, caregiverId, taskId, taskName, taskNote,
                            assignedStatus, completionStatus, roomStr);
                    tasks.add(t);
                }
            }
        }
        return tasks;
    }

    /** Display caregivee on the main screen. */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void displayCaregivee(){
        ArrayList<String> listGroup = new ArrayList<>();
        HashMap<String,ArrayList<String>> listChild = new HashMap<>();
        caregiveeInfo.forEach((id, name) -> {
            listGroup.add(name);
            List < Task > tasks = taskList.get(id);
            ArrayList < String > taskName =  new ArrayList<>();
            if (tasks != null){
                for (Task task : tasks){
                    taskName.add("    " + task.taskName.replace("\"", ""));
                }
            }
            listChild.put(name, taskName);
        });
        adapter = new MainAdapter(listGroup, listChild);

        caregiveeList.setAdapter(adapter);
        caregiveeList.setOnChildClickListener(((parent, v, groupPosition, childPosition, id) -> {
            Task myTask = null;
            String currTask = listChild.get(listGroup.get(groupPosition)).get(childPosition);
            currTask = currTask.trim();
            String room = "", cid = "";
            for(String caregiveeId : taskList.keySet()){
                if(caregiveeInfo.get(caregiveeId).equals(listGroup.get(groupPosition))
                        && taskList.containsKey(caregiveeId)){
                    List<Task> tasks = taskList.get(caregiveeId);
                    for(Task t : tasks){
                        if(t.taskName.equals(currTask)){
                            room = t.room;
                            cid = caregiveeId;
                            myTask = t;
                        }
                    }
                }
            }
//            for(Task task : Objects.requireNonNull(taskList.get(listGroup.get(groupPosition)))){
//                if(task.taskName.equals(currTask)){
//                    room = task.room;
//                }
//            }
//            for(Task task : taskList.get(listChild.get(groupPosition))){

//            }
////            String[] task = taskName.split("\n"); // separate task name & room
////            for(int i = 0; i < task.length; i++){ // remove spaces
////                task[i] = task[i].trim();
////            }
////            if(task.length == 2) {
                Intent intent = new Intent(getContext(), EditTask.class);
                intent.putExtra("task", currTask);
                intent.putExtra("room", room);
                intent.putExtra("caregiveeid", cid);
                intent.putExtra("currtask", (Parcelable) myTask);
                if(myTask == null){ return false; }
                List<String> currRooms = caregiveeRooms.get(myTask.caregiveeId);
                if(currRooms == null){ return false; }
                String[] currCaregiveeRooms = currRooms.toArray(new String[currRooms.size()]);
                intent.putExtra("rooms", currCaregiveeRooms);
                startActivity(intent);
                Log.i("INFO", "task = " + currTask + " room = " + room);
                return true;
//            }
//            return false;
        }));

        // Store caregivee + their name, and caregivee + their rooms for the Add Task page
        shareDataWithAddTask();



    }

    public void selectTask(){
        caregiveeList.setOnItemClickListener(((parent, view1, position, id) ->
        {
            Log.i("INFO", "parent, position, id = " + parent.toString() + position + id);
        }));
    }

    /** Sent the data of the caregivee, their name, and their rooms to the Add Task page. */
    public void shareDataWithAddTask(){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        SharedPreferences.Editor editor = preferences.edit();
        Gson gson = new Gson();
        if (caregiveeInfo != null && caregiveeRooms != null){
            String caregiveeInfoStr = gson.toJson(caregiveeInfo);
            String caregiveeRoomStr = gson.toJson(caregiveeRooms);
            editor.putString("caregiveeInfo", caregiveeInfoStr);
            editor.putString("caregiveeRoom", caregiveeRoomStr);
            editor.apply();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_task, container, false);

        // Get the expandable list id
        caregiveeList = (ExpandableListView) view.findViewById(R.id.caregiveelist);

        // Query caregivees for this caregiver
        queryCaregivees();

        // Redirect to add task page for floating + button
        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.addTaskButton);
        button.setOnClickListener(new View.OnClickListener() {@Override
        public void onClick(View view) {
            startActivity(new Intent(view.getContext(), AddTask.class));
        }
        });

        return view;
    }
}