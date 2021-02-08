package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Task_Caregivee_SingleView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Task_Caregivee_SingleView extends Fragment {

    public Task_Caregivee_SingleView() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static Task_Caregivee_SingleView newInstance(String param1, String param2) {
        Task_Caregivee_SingleView fragment = new Task_Caregivee_SingleView();
        Bundle args = new Bundle();
        fragment.setArguments(args);
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
        return inflater.inflate(R.layout.fragment_task__caregivee__single_view, container, false);
    }
}