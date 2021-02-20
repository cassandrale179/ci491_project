package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeCaregiver#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeCaregiver extends Fragment {

    // Set global variables
    ExpandableListView caregiveeList;
    MainAdapter adapter;


    public HomeCaregiver() { }


    public static HomeCaregiver newInstance() {
        HomeCaregiver fragment = new HomeCaregiver();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void displayCaregiveeList(){
        ArrayList<String> listGroup = new ArrayList<>();
        HashMap<String, ArrayList<String>> listChild = new HashMap<>();
        listGroup.add("Britney Spears");
        listGroup.add("Hannah Montana");
        listGroup.add("Alex Russo");

        listGroup.forEach(caregivee -> {
            ArrayList<String> listChildValues = new ArrayList<String>(
                    Arrays.asList("View Profile", "See Progress", "Set Tasks", "Delete Caregivee"));
            listChild.put(caregivee, listChildValues);
        });
        adapter = new MainAdapter(listGroup, listChild);
        caregiveeList.setAdapter(adapter);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_home_caregiver, container, false);

        caregiveeList = (ExpandableListView) view.findViewById(R.id.caregiveeHomelist);

        displayCaregiveeList();

        return view;
    }
}