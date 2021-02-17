package com.example.caregiver;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetTasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetTasksFragment extends Fragment {

    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private String caregiveeID;
    private ArrayList<Task> tasks;
    private String caregiverName;
    private int numTasks = 0;
    private int tasksAssigned = 0;
    private ArrayCheckboxAdapter<Task> listAdapter;

    public SetTasksFragment(String caregiveeID) {
        // Required empty public constructor
        this.caregiveeID = caregiveeID;
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SetTasksFragment.
     */


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    private Task[] getRoomTasks(DataSnapshot roomSnapshot)
    {
        ArrayList<Task> tasks = new ArrayList<>();
        for (DataSnapshot task : roomSnapshot.child("tasks").getChildren())
        {
            String name = task.child("name").getValue().toString();
            String id = task.getKey();
            boolean status = task.child("assignedStatus").getValue().equals("true");
            String room = roomSnapshot.getKey();
            Task t = new Task(id, name, status, room);
            tasks.add(t);
            numTasks++;
            if (status)
            {
                tasksAssigned++;
            }
        }
        return tasks.toArray(new Task[tasks.size()]);
    }

    private void updateNumSelectedText()
    {
        View view = getView();
        TextView numSelectedText = view.findViewById(R.id.textView3);
        numSelectedText.setText(tasksAssigned + " of " + numTasks + " tasks assigned");
    }

    private void getAllCaregiveeTasks(String caregiveeID)
    {
        final DatabaseReference rooms = database.child("/users/" + caregiveeID + "/rooms");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                numTasks = 0;
                tasksAssigned = 0;
                listAdapter.clear();
                for (DataSnapshot roomSnapshot: snapshot.getChildren())
                {
                    Task[] tasks = getRoomTasks(roomSnapshot);
                    for (Task task : tasks)
                    {
                        listAdapter.add(task, task.getAssignedStatus());
                    }
                }
                updateNumSelectedText();
                listAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAIL", "getAllTasks:onCancelled", error.toException());
            }
        };
        rooms.addValueEventListener(valueEventListener);
    }

    private void updateViewWithCorrectCaregiveeName(View rootView)
    {
        final DatabaseReference name = database.child("/users/" + caregiveeID + "/name");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                caregiverName = snapshot.getValue().toString();
                // Set the text correctly
                TextView assignedTasksText = rootView.findViewById(R.id.textView);
                assignedTasksText.setText("Assigned tasks for " + caregiverName);
                TextView nameText = rootView.findViewById(R.id.textView2);
                nameText.setText(caregiverName);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAIL", "getCaregiverName:onCancelled", error.toException());
            }
        };
        name.addValueEventListener(valueEventListener);
    }

    public class Task {

        private String taskID;
        private String name;
        private boolean assignedStatus;
        private String room;

        public Task(String id, String name, boolean assignedStatus, String room)
        {
            this.taskID = id;
            this.name = name;
            this.assignedStatus = assignedStatus;
        }

        public String getTaskID() {
            return taskID;
        }

        public String getName() {
            return name;
        }

        public String getRoom() {
            return room;
        }

        public boolean getAssignedStatus() {
            return assignedStatus;
        }

        public void toggleAssignedStatus()
        {
            assignedStatus = !assignedStatus;
        }

        @Override
        public String toString()
        {
            return name;
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_tasks, container, false);

        updateViewWithCorrectCaregiveeName(view);

        // Set the content of the ListView
        ListView listView = (ListView)view.findViewById(R.id.setTasksListView);
        tasks = new ArrayList<>();
        listAdapter = new ArrayCheckboxAdapter<Task>(getContext(),
                android.R.layout.simple_list_item_1, tasks);
        getAllCaregiveeTasks(caregiveeID);
        listView.setAdapter(listAdapter);


        // Make the button do something.
        Button button = view.findViewById(R.id.assignTasksButton);
        button.setOnClickListener(v -> {
            ArrayList<Task> selVals = listAdapter.getSelectedObjects();

            for (Task task : selVals)
            {
                DatabaseReference statusRef = database.child("/users/" + caregiveeID + "/name/rooms/" + task.getRoom()
                        + "/tasks/" + task.getTaskID() + "/assignedStatus");
                statusRef.setValue(task.getAssignedStatus() ? "true" : "false");
            }
        });

        return view;
    }
}