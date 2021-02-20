package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeCaregiver#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeCaregiver extends Fragment {


    public HomeCaregiver() {
        // Required empty public constructor
    }


    public static HomeCaregiver newInstance() {
        HomeCaregiver fragment = new HomeCaregiver();
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
        return inflater.inflate(R.layout.fragment_home_caregiver, container, false);
    }
}