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

import com.example.caregiver.model.Task;
import com.example.caregiver.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Create the Tasks page on the Caregiver view. Display list of caregivees
 * with their tasks.
 */
public class TaskFragment extends Fragment {
    // Global object representation of the view.
    View view;

    // This stores the R.android.id of the expandable list on Tasks page
    ExpandableListView caregiveeList;

    // Global reference to Firebase
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // This hashmap store the caregivee id (key) and their tasks (value)
    HashMap< String, List <Task>> taskList = new HashMap<>();

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
                String caregiveeName = caregivee.getValue().toString();
                long size = snapshot.child("caregivees").getChildrenCount();
                getCaregiveeNameAndTask(caregiveeId, caregiveeName, size);
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
    protected void getCaregiveeNameAndTask(String caregiveeId, String caregiveeName, long size) {
        Task taskModelObject = new Task();
        taskModelObject.getAllTasks(caregiveeId, new App.TaskCallback() {
            @Override
            public void onDataGot(List<Task> tasks){
                List<String> rooms = tasks.stream()              // stream over the list
                        .map(task -> task.room)                  // try to get the rooms
                        .collect(Collectors.toCollection(ArrayList::new));
                caregiveeRooms.put(caregiveeId, rooms);
                taskList.put(caregiveeId, tasks);
                caregiveeInfo.put(caregiveeId, caregiveeName);
                if (caregiveeInfo.size() == size){
                    displayCaregivee();
                }
            }
        });
    }

    /**
     * Finds the selected task in list of tasks
     * @param currCaregiveeName, selected caregivee's name
     * @param currTaskName, selected task for selected caregivee
     * @return selected task object, null if no such object exists
     */
    private Task getSelectedTask(String currCaregiveeName, String currTaskName){
        for(String caregiveeId : taskList.keySet()){
            String caregiveeName = caregiveeInfo.get(caregiveeId);
            if(caregiveeName != null && caregiveeName.equals(currCaregiveeName)
                    && taskList.containsKey(caregiveeId)){
                List<Task> allTasks = taskList.get(caregiveeId);
                for(Task task : allTasks){
                    if(task.taskName.equals(currTaskName)){
                        return task;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Extracts task room & caregiveeId info & passes to new EditTask intent
     * @param selectedTask, task to pass into intent
     * @return true if successful retrieval of info
     */
    private boolean createEditTaskIntent(Task selectedTask){
        List<String> currRooms = caregiveeRooms.get(selectedTask.caregiveeId);

        if(currRooms == null){
            return false;
        }
        String[] currCaregiveeRooms = new String[currRooms.size()];
        currCaregiveeRooms = currRooms.toArray(currCaregiveeRooms);

        String carevigeeName = caregiveeInfo.get(selectedTask.caregiveeId);

        // create new intent, pass curr task, caregivee name & rooms
        Intent intent = new Intent(getContext(), EditTask.class);
        intent.putExtra("currTask", selectedTask);
        intent.putExtra("rooms", currCaregiveeRooms);
        intent.putExtra("caregiveeName", carevigeeName);

        startActivity(intent);
        return true;
    }

    /** Display caregivee on the main screen. */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void displayCaregivee(){
        HashMap<String,ArrayList<String>> listChild = new HashMap<>();
        ArrayList<String> caregiveeNames = new ArrayList<>();
        ArrayList<User> caregivees = new ArrayList<>();
        caregiveeInfo.forEach((id, name) -> {
            User caregivee = new User(id, name);
            caregivees.add(caregivee);
        });
        // Sort caregivee list by name
        Collections.sort(caregivees, (User a, User b) -> a.name.compareToIgnoreCase(b.name));
        caregivees.forEach(caregivee -> {
            caregiveeNames.add(caregivee.name);
            List < Task > tasks = taskList.get(caregivee.id);
            ArrayList < String > taskName =  new ArrayList<>();
            if (tasks != null){
                for (Task task : tasks){
                    taskName.add("    " + task.taskName.replace("\"", ""));
                }
            }
            listChild.put(caregivee.name, taskName);
        });

        adapter = new MainAdapter(caregiveeNames, listChild);
        caregiveeList.setAdapter(adapter);

        // Set listener on task click to edit task.
        caregiveeList.setOnChildClickListener(((parent, v, groupPosition, childPosition, id) -> {
            Log.d("call!", "listener should be call");
            // get selected task info
            String currCaregiveeName = caregiveeNames.get(groupPosition);
            String currTaskName = listChild.get(currCaregiveeName).get(childPosition);
            currTaskName = currTaskName.trim(); // remove whitespaces
            Task selectedTask = getSelectedTask(currCaregiveeName, currTaskName);
            if(selectedTask == null) {
                Log.e("FAIL", "TaskFragment:displayCaregivee could not get selectedTask.");
                return false;
            }

            // create edit task intent with selected task
            return createEditTaskIntent(selectedTask);
        }));

        // Store caregivee + their name, and caregivee + their rooms for the Add Task page
        shareDataWithAddTask();
    }

    /** Sent the data of the caregivee, their name, and their rooms to the Add Task page. */
    public void shareDataWithAddTask() {
        if (this.getContext() != null) {
            SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this.getContext());
            SharedPreferences.Editor editor = preferences.edit();
            Gson gson = new Gson();
            if (caregiveeInfo != null && caregiveeRooms != null) {
                String caregiveeInfoStr = gson.toJson(caregiveeInfo);
                String caregiveeRoomStr = gson.toJson(caregiveeRooms);
                editor.putString("caregiveeInfo", caregiveeInfoStr);
                editor.putString("caregiveeRooms", caregiveeRoomStr);
                editor.apply();
            }
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