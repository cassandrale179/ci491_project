package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Task_Caregivee#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Task_Caregivee extends Fragment {

    public Task_Caregivee() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Task_Caregivee newInstance(String param1, String param2) {
        Task_Caregivee fragment = new Task_Caregivee();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    protected void loadCaregiveesTask(){


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task__caregivee, container, false);

        // Add list
        ArrayList<String> arrayList = new ArrayList<String>();
        arrayList.add("Brush Your Teeth");
        arrayList.add("Wash Your Hand");
        arrayList.add("Turn Off The Light");
        arrayList.add("Lock the Door");
        arrayList.add("Do Your Laundry");
        arrayList.add("Set Alarm for Morning");

        List<Map<String, String>> data = new ArrayList<Map<String, String>>();
        for (int i=0; i <arrayList.size(); i++) {
            Map<String, String> datum = new HashMap<String, String>(2);
            datum.put("title", arrayList.get(i));
            datum.put("subtitle", "bathroom");
            data.add(datum);
        }
        final ListView list = view.findViewById(R.id.caregiveeTaskList);
        SimpleAdapter adapter = new SimpleAdapter(getActivity(), data,
                android.R.layout.simple_list_item_2,
                new String[] {"title", "subtitle"},
                new int[] {android.R.id.text1,
                        android.R.id.text2});
        list.setAdapter(adapter);

        return view;
    }
}