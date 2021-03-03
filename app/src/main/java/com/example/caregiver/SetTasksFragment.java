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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Set Task fragment to assign task to caregivees.
 */
public class SetTasksFragment extends Fragment {

    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    private String caregiveeID;
    private ArrayList<Task> tasks;
    private String caregiverName;
    private int numTasks = 0;
    private int tasksAssigned = 0;
    private ArrayCheckboxAdapter<Task> listAdapter;
    private View fragmentView;

    public SetTasksFragment(String caregiveeID) {
        this.caregiveeID = caregiveeID;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private Task[] getRoomTasks(DataSnapshot roomSnapshot)
    {
        ArrayList<Task> tasks = new ArrayList<>();
        for (DataSnapshot task : roomSnapshot.child("tasks").getChildren()) {
            String name = "   " + task.child("name").getValue().toString();
            String id = task.getKey();
            boolean status = task.child("assignedStatus").getValue().equals(true);
            String room = roomSnapshot.getKey();
            Task taskObject = new Task(id, name, status, room);
            tasks.add(taskObject);
            numTasks++;
            if (status) {
                tasksAssigned++;
            }
        }
        return tasks.toArray(new Task[tasks.size()]);
    }

    private void updateNumSelectedText()
    {
        TextView numSelectedText = fragmentView.findViewById(R.id.textView3);
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
                for (DataSnapshot roomSnapshot: snapshot.getChildren()) {
                    Task[] tasks = getRoomTasks(roomSnapshot);
                    for (Task task : tasks) {
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

        public Task(String id, String name, boolean assignedStatus, String room) {
            this.taskID = id;
            this.name = name;
            this.assignedStatus = assignedStatus;
            this.room = room;
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


        // Update assigned status for assigned tasks
        Button button = view.findViewById(R.id.assignTasksButton);
        button.setOnClickListener(v -> {
            List<Task> tasks = listAdapter.getObjects();
            for (Task task : tasks) {
                DatabaseReference taskRef = database
                        .child("users").child(caregiveeID)
                        .child("rooms").child(task.getRoom())
                        .child("tasks").child(task.getTaskID());
                boolean isSelected = listAdapter.getSelectedObjects().contains(task);
                taskRef.updateChildren(Collections.singletonMap("assignedStatus", isSelected));
                if(isSelected != task.assignedStatus) {
                    task.toggleAssignedStatus();
                }
            }
        });
        fragmentView = view;
        return view;
    }
}