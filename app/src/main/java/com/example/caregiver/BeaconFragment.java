package com.example.caregiver;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.example.caregiver.services.BeaconScanService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kontakt.sdk.android.ble.device.BeaconRegion;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Objects;
import java.util.UUID;

public class BeaconFragment extends Fragment {

    public static final HashMap<String, HashMap<String, Double>> regionRssiMap = new HashMap<>();
    public static String regionName;
    public static int regionMajorValue;
    public static String kontaktUUID;
    public static Collection<IBeaconRegion> beaconRegions = new ArrayList<>();
    // Variables pointing to field names
    public EditText UUIDField; //check
    public EditText regionNameField;
    public EditText majorField;


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
        // Once beacon regions are extracted from firebase database, BeaconScanService is started
        startScanButton.setOnClickListener(v -> getBeaconRegions(scanServiceIntent));

        Button stopScanButton = rootView.findViewById(R.id.stop_button);
        stopScanButton.setOnClickListener(v -> stopBeaconScanService(scanServiceIntent));

        Button addRegionButton = rootView.findViewById(R.id.add_region);
        addRegionButton.setOnClickListener(v -> addRegion(rootView));

        return rootView;

    }

    private void getBeaconRegions(Intent scanServiceIntent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users/" + user.getUid() + "/rooms");

        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> newRegions = dataSnapshot.getChildren();
//                kontaktUUID = dataSnapshot.child("uuid").getValue().toString();
                kontaktUUID = "f7826da6-4fa2-4e98-8024-bc5b71e0893e";
                for (DataSnapshot ds : newRegions) {
                    regionName = ds.getKey();
                    for (DataSnapshot child : ds.getChildren()){
                        if (child.getKey().equals("beaconMajor")){
                            regionMajorValue = Integer.parseUnsignedInt((String) child.getValue());
                        }
                    }

                    IBeaconRegion region = new BeaconRegion.Builder()
                            .identifier(regionName)
                            .proximity(UUID.fromString(kontaktUUID))
                            .major(regionMajorValue).build();

                    beaconRegions.add(region);
                    Log.i("BeaconFragment", "Region retrieved " + region.toString());

                    regionRssiMap.put(regionName, new HashMap<String, Double>());
                    regionRssiMap.get(regionName).put("sum", 0.0);
                    regionRssiMap.get(regionName).put("count", 0.0);
                    regionRssiMap.get(regionName).put("dist", 0.0);
                }
                // All beacon regions are available from firebase, starting BeaconScanService
                startBeaconScanService(scanServiceIntent);
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.d("failure", "Unable to obtain user information");
            }
        });

    }

    public void startBeaconScanService(Intent scanServiceIntent) {
        getActivity().startService(scanServiceIntent);
    }

    public void stopBeaconScanService(Intent scanServiceIntent) {
        getActivity().stopService(scanServiceIntent);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addRegion(View rootView) {

        UUIDField = rootView.findViewById(R.id.UUIDText);
        String UUIDValue = UUIDField.getText().toString();

        regionNameField = rootView.findViewById(R.id.regionName);
        String regionName = regionNameField.getText().toString();

        majorField = rootView.findViewById(R.id.major);
        String majorValue = majorField.getText().toString();

        if (UUIDValue.isEmpty() || regionName.isEmpty() || majorValue.isEmpty()) {
            displayErrorMessage("One or more fields are empty.", rootView);
        } else {
            displayErrorMessage("", rootView);
            regionInfo newRegionInfo = new regionInfo(UUIDValue, regionName, majorValue);
            updateRegionInfoInBackend(newRegionInfo);
            Log.i("BeaconFragment", "New Region Added " + newRegionInfo.toString());
        }

    }

    public void updateRegionInfoInBackend(regionInfo newRegionInfo) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> newRegion = new HashMap<String, Object>();
        newRegion.put(newRegionInfo.regionName, newRegionInfo.majorValue);
        database.child("users").child(user.getUid()).child("rooms").updateChildren(newRegion);
    }

    public void displayErrorMessage(String sourceString, View rootView) {
        TextView textView = rootView.findViewById(R.id.addRegionMessage);
        textView.setText(Html.fromHtml(sourceString));
        textView.setVisibility(View.VISIBLE);
    }

    public static class regionInfo {
        public String UUID;
        public String regionName;
        public String majorValue;

        public regionInfo(String UUID, String regionName, String majorValue) {
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


}