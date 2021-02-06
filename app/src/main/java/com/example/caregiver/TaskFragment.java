package com.example.caregiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

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
    public class Task {
        String caregiveeId; /* the caregivee associated with the task */
        String caregiverId; /* the caregiver who assigned the task */
        String taskId; /* the task id, auto-generated by Firebase */
        String taskName; /* task actions (e.g Brush Your Teeth) */
        String taskNote; /* task notes (Your Brush Is The Red One) */
        String assignedStatus; /* true = task assigned to caregivee to do, false otherwise*/
        String completionStatus; /* complete = caregivee complete the tasks, incomplete otherwise */

        // Default constructor
        public Task() {};

        // Alternative constructor to instantiate the task object.
        public Task(String caregiveeId, String caregiverId, String taskId, String taskName, String taskNote,
                    String assignedStatus, String completionStatus) {
            this.caregiveeId = caregiveeId;
            this.caregiverId = caregiverId;
            this.taskId = taskId;
            this.taskName = taskName;
            this.taskNote = taskNote;
            this.completionStatus = completionStatus;
            this.assignedStatus = assignedStatus;
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
     * @param view The view of the task page
     */
    public void queryCaregivees(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = preferences.getString("userId", "");
        DatabaseReference ref = database.child("users/" + userId);
        caregiveeList = (ExpandableListView) view.findViewById(R.id.caregiveelist);

        ref.addValueEventListener(new ValueEventListener() {@Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String value = snapshot.child("caregivees").getValue().toString();
            List < String > caregivees = Arrays.asList(value.split("\\s*,\\s*"));
            for (int i = 0; i < caregivees.size(); i++) {
                getCaregiveeNameAndTask(caregivees.get(i), caregivees.size());
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
    protected void getCaregiveeNameAndTask(String caregiveeId, int size) {
        DatabaseReference ref = database.child("users/" + caregiveeId);
        JsonObject caregivee = new JsonObject();
        ref.addValueEventListener(new ValueEventListener() {@RequiresApi(api = Build.VERSION_CODES.N)@Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String name = dataSnapshot.child("name").getValue().toString();
            Object taskObject = dataSnapshot.child("rooms").getValue();
            if (taskObject != null) {
                Gson gson = new Gson();
                String tasksJson = gson.toJson(taskObject);
                List<Task> tasks = createRoomAndTaskObject(caregiveeId, tasksJson);
                caregiveeInfo.put(caregiveeId, name);
                taskList.put(caregiveeId, tasks);
            }

            int s = caregiveeInfo.size();
            Log.d("caregiveeinfosize", String.valueOf(s));

            // TODO: hacky way of display the list. Need to use async.
            if (caregiveeInfo.size() == size){
                Log.d("huh", "THIS SHOULD BE CALL!");
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
                    String caregiverId = task.get("caregiverID").toString();
                    String taskName = task.get("name").toString();
                    String taskNote = task.get("notes").toString();
                    String assignedStatus = task.get("assignedStatus").toString();
                    String completionStatus = task.get("completionStatus").toString();
                    Task t = new Task(caregiveeId, caregiverId, taskId, taskName, taskNote,
                            assignedStatus, completionStatus);
                    tasks.add(t);
                }
            }
        }
        return tasks;
    }

    /**
     * Display caregivee on the main screen.
     */
    public void displayCaregivee(){
        Log.d("call this!", caregiveeInfo.toString());
        Log.d("yahoo!", taskList.toString());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_task, container, false);

        // Query caregivees for this caregiver
        queryCaregivees(view);

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