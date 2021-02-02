package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {

    ExpandableListView caregiveeList;
    ArrayList<String> listGroup = new ArrayList<>();
    HashMap<String, ArrayList<String>> listChild = new HashMap<>();
    MainAdapter adapter;

    public TaskFragment() {
        // Required empty public constructor
    }


    // TODO: Rename and change types and number of parameters
    public static TaskFragment newInstance(String param1, String param2) {
        TaskFragment fragment = new TaskFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        // Set up the child list view
        ArrayList<String> fakeNames = new ArrayList<String>();
        fakeNames.add("Mary Yu");
        fakeNames.add("John Smith");
        fakeNames.add("Robert Nguyen");

        ArrayList<String> fakeTasks = new ArrayList<String>();
        fakeTasks.add("Brush Your Teeth");
        fakeTasks.add("Wash Your Hand");
        fakeTasks.add("Do Your Laundry");

        caregiveeList = (ExpandableListView) view.findViewById(R.id.caregiveelist);
        for (int g = 0; g < fakeNames.size(); g++){
            listGroup.add(fakeNames.get(g));
            ArrayList<String> arrayList = new ArrayList<>();
            for (int c = 0; c < fakeTasks.size(); c++){

                // TODO: this is a brute force attempt to set left margin to child text
                // for some reason I can't set it in MainAdapter.
                // If someone can fix this, that would be great.
               arrayList.add("    " + fakeTasks.get(c));
            }
            listChild.put(listGroup.get(g), arrayList);
        }

        adapter = new MainAdapter(listGroup, listChild);
        caregiveeList.setAdapter(adapter);
        return view;
    }
}