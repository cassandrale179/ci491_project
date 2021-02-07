package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Task_Caregivee#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Task_Caregivee extends Fragment {

    public Task_Caregivee() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment Task_Caregivee.
     */
    // TODO: Rename and change types and number of parameters
    public static Task_Caregivee newInstance(String param1, String param2) {
        Task_Caregivee fragment = new Task_Caregivee();
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
        return inflater.inflate(R.layout.fragment_task__caregivee, container, false);
    }
}