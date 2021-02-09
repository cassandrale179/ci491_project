package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.WrapperListAdapter;

import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SetTasksFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SetTasksFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public SetTasksFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment SetTasksFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static SetTasksFragment newInstance(String param1, String param2) {
        SetTasksFragment fragment = new SetTasksFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_set_tasks, container, false);


        // Set the content of the ListView
        ListView listView = (ListView)view.findViewById(R.id.setTasksListView);
        String[] testData = {"One", "Two", "Three"};
        ArrayCheckboxAdapter<String> testAdapter = new ArrayCheckboxAdapter<String>(getContext(),
                android.R.layout.simple_list_item_1, testData);
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