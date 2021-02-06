package com.example.caregiver;

import android.content.ContentProviderClient;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import com.example.caregiver.services.BeaconScanService;

import java.util.HashMap;

public class BeaconFragment extends Fragment {

    // Variables pointing to field names
    public EditText UUIDField; //check
    public EditText regionNameField;
    public EditText majorField;
    public EditText minorField;


    public static class regionInfo {
        public String UUID; 
        public String regionName;
        public String majorValue;

        public regionInfo(String UUID, String regionName, String majorValue){
            this.UUID = UUID;
            this.regionName = regionName;
            this.majorValue = majorValue;
        }

        @Override
        public String toString() {
            return "regionInfo{" +
                    "UUID='" + UUID + '\'' +
                    ", regionName='" + regionName + '\'' +
                    ", majorValue=" + majorValue +
                    '}';
        }
    }

    public BeaconFragment() {
        // Required empty public constructor
    }

    public static BeaconFragment newInstance() {
        return new BeaconFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_beacon, container, false);
        Intent scanServiceIntent = new Intent(getActivity(), BeaconScanService.class);

        Button startScanButton = rootView.findViewById(R.id.start_button);
        startScanButton.setOnClickListener(v -> startBeaconScanService(scanServiceIntent));

        Button stopScanButton = rootView.findViewById(R.id.stop_button);
        stopScanButton.setOnClickListener(v -> stopBeaconScanService(scanServiceIntent));

        Button addRegionButton = rootView.findViewById(R.id.add_region);
        addRegionButton.setOnClickListener(v -> addRegion(rootView));

        return rootView;

    }

    public void startBeaconScanService(Intent scanServiceIntent){
        getActivity().startService(scanServiceIntent);
    }

    public void stopBeaconScanService(Intent scanServiceIntent){
        getActivity().stopService(scanServiceIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addRegion(View rootView){
        
        UUIDField = rootView.findViewById(R.id.UUIDText);
        String UUIDValue = UUIDField.getText().toString();

        regionNameField = rootView.findViewById(R.id.regionName);
        String regionName = regionNameField.getText().toString();

        majorField = rootView.findViewById(R.id.major);
        String majorValue = majorField.getText().toString();

        if (UUIDValue.isEmpty() || regionName.isEmpty() || majorValue.isEmpty()){
            displayErrorMessage("One or more fields are empty.", rootView);
        }
        else {
            displayErrorMessage("", rootView);
            regionInfo newRegionInfo = new regionInfo(UUIDValue, regionName, majorValue);
            Log.i("Sample", "region Info = " + newRegionInfo.toString());
        }

    }

    public void updateRegionInfoInBackend(regionInfo newRegionInfo){
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> newRegion = new HashMap<String, Object>();
        newRegion.put(newRegionInfo.regionName, newRegionInfo.majorValue);
        database.child("users").child(user.getUid()).child("rooms").updateChildren(newRegion);
    }

    public void displayErrorMessage(String sourceString, View rootView){
        TextView textView = rootView.findViewById(R.id.addRegionMessage);
        textView.setText(Html.fromHtml(sourceString));
        textView.setVisibility(View.VISIBLE);
    }


}