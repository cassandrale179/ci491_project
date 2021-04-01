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
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.example.caregiver.model.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskCaregivee#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskCaregivee extends Fragment {

    // Global object to store the task list
    List <Task> taskList = new ArrayList < >();

    // Global view
    View view;


    public TaskCaregivee() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static TaskCaregivee newInstance(String param1, String param2) {
        TaskCaregivee fragment = new TaskCaregivee();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /** Loads all rooms associated with this caregivee */
    protected void loadCaregiveesTask() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String caregiveeId = preferences.getString("userId", "");
        if (caregiveeId.isEmpty()) {
            return;
        }

        // This implement the asynchronous call method to get Tasks using Customized Call back
        // See: https://stackoverflow.com/questions/51402623/how-to-wait-for-an-asynchronous-method
        Task taskModelObject = new Task();
        taskModelObject.getAllTasks(caregiveeId, new App.TaskCallback() {
            @Override
            public void onDataGot(List<Task> tasks){
                Log.d("tasks", tasks.toString());
            }
        });
    }

    protected void displayTaskList(List < Task > tasks) {
        List < Map < String,  String >> data = new ArrayList < Map < String, String >> ();
        for (Task t: tasks) {
            Map < String,
                    String > room = new HashMap < String,
                    String > (2);
            room.put("title", t.taskName);
            room.put("subtitle", t.room);
            data.add(room);
        }
        final ListView list = view.findViewById(R.id.caregiveeTaskList);
        if(getActivity() != null) {
            SimpleAdapter adapter = new SimpleAdapter(
                    getActivity(), data, android.R.layout.simple_list_item_2, new String[]{
                    "title",
                    "subtitle"
            },
                    new int[]{
                            android.R.id.text1,
                            android.R.id.text2
                    });
            list.setAdapter(adapter);

            // Redirect to TaskSingleView page with the task data
            list.setOnItemClickListener((parent, view, position, id) -> {
                Gson gson = new Gson();
                if (taskList.get(position) != null) {
                    String taskJson = gson.toJson(taskList.get(position));
                    Intent i = new Intent(view.getContext(), TaskSingleView.class);
                    i.putExtra("taskObject", taskJson);
                    startActivity(i);
                }
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        view = inflater.inflate(R.layout.fragment_task__caregivee, container, false);

        // Load the caregivee list of task for the day
        loadCaregiveesTask();

        return view;
    }
}