package com.example.caregiver;

import android.content.ContentProviderClient;
import android.content.Intent;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.example.caregiver.services.BeaconScanService;

/**
 * Start Scanning button 
 * 
 */

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BeaconFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeaconFragment extends Fragment {

    // Variables pointing to field names
    public EditText UUIDField; //check
    public EditText regionNameField;
    public EditText majorField;
    public EditText minorField; 

    public class regionInfo {
        public String UUID; 
        public String regionName;
        public String majorValue;
        public String minorValue;

        public regionInfo(String UUID, String regionName, String majorValue, String minorValue){
            this.UUID = UUID;
            this.regionName = regionName;
            this.majorValue = majorValue;
            this.minorValue = minorValue;
        }
    }

    public BeaconFragment() {
        // Required empty public constructor
    }

    public static BeaconFragment newInstance() {
        BeaconFragment fragment = new BeaconFragment();
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
        View rootView = inflater.inflate(R.layout.fragment_beacon, container, false);
        Button startScanButton = (Button) rootView.findViewById(R.id.start_button);
        Intent scanServiceIntent = new Intent(getActivity(), BeaconScanService.class);
        startScanButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity().startService(scanServiceIntent);
            }
        });

        Button stopScanButton = (Button) rootView.findViewById(R.id.stop_button);
        stopScanButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                getActivity().stopService(scanServiceIntent);
            }
        });

        return rootView;
    }


}