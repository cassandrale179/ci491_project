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
    private ArrayList<String> taskNames;
    private String caregiverName;

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

    private String[] getRoomTasks(DataSnapshot roomSnapshot)
    {
        ArrayList<String> tasks = new ArrayList<>();
        for (DataSnapshot task : roomSnapshot.child("tasks").getChildren())
        {
            tasks.add(task.child("name").getValue().toString());
        }
        return tasks.toArray(new String[tasks.size()]);
    }

    private void getAllCaregiveeTasks(String caregiveeID, ArrayAdapter adapter)
    {
        final DatabaseReference rooms = database.child("/users/" + caregiveeID + "/rooms");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomSnapshot: snapshot.getChildren())
                {
                    String[] tasks = getRoomTasks(roomSnapshot);
                    for (String task : tasks)
                    {
                        adapter.add(task);
                    }
                }
                adapter.notifyDataSetChanged();
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_tasks, container, false);

        updateViewWithCorrectCaregiveeName(view);

        // Set the content of the ListView
        ListView listView = (ListView)view.findViewById(R.id.setTasksListView);
        taskNames = new ArrayList<>();
        ArrayCheckboxAdapter<String> testAdapter = new ArrayCheckboxAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, taskNames);
        getAllCaregiveeTasks(caregiveeID, testAdapter);
        listView.setAdapter(testAdapter);


        // Make the button do something.
        Button button = view.findViewById(R.id.assignTasksButton);
        button.setOnClickListener(v -> {
            ListAdapter adapter = listView.getAdapter();
            if (adapter instanceof WrapperListAdapter)
            {
                adapter = ((WrapperListAdapter) adapter).getWrappedAdapter();
            }
            ArrayList<String> selVals = ((ArrayCheckboxAdapter<String>)adapter).getSelectedObjects();

            // Add a TextView so we can see if it worked
            LinearLayout vert = (LinearLayout)view.findViewById(R.id.setTasksVertLayout);
            TextView text = new TextView(getContext());
            String displayText = "You selected: ";
            for (String s : selVals)
            {
                displayText += s + ", ";
            }
            displayText = displayText.substring(0, displayText.length()-2);
            text.setText(displayText);
            vert.addView(text);
        });

        return view;
    }
}