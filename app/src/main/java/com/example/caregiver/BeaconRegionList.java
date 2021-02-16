package com.example.caregiver;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TableLayout;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BeaconRegionList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeaconRegionList extends Fragment {

    public BeaconRegionList() {
        // Required empty public constructor
    }

    public static BeaconRegionList newInstance(String param1, String param2) {
        BeaconRegionList fragment = new BeaconRegionList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) { super.onCreate(savedInstanceState);}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_beacon_region_list, container, false);
        TableLayout regionTable = (TableLayout) view.findViewById(R.id.regionTable);

        return null;
    }
}