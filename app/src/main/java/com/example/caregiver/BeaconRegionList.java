package com.example.caregiver;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BeaconRegionList#newInstance} factory method to
 * create an instance of this fragment.
 */

public class BeaconRegionList extends Fragment {

    public static final HashMap<String, HashMap<String, Double>> regionRssiMap = new HashMap<>();
    public static HashMap<String, Integer> regionMajorMap = new HashMap<String, Integer>();
    public static String regionName;
    public static int regionMajorValue;
    public static String kontaktUUID;
    public static Collection<IBeaconRegion> beaconRegions = new ArrayList<>();
    public TableLayout regionTable;
    public static final int LOCATION_REQUEST_CODE = 100;

    public BeaconRegionList() {
        // Required empty public constructor
    }

    public static BeaconRegionList newInstance() {
        BeaconRegionList fragment = new BeaconRegionList();
        return fragment;
    }

    /**
     * Default onCreate function
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        getKontaktUUID();
        requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_REQUEST_CODE);
        super.onCreate(savedInstanceState);
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch(requestCode){
            case LOCATION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    Toast.makeText(getContext(), "Location permission granted", Toast.LENGTH_SHORT).show();
                }
                else {
                    Toast.makeText(getContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
                }
        }

    }


    /**
     * Populates the table view with the region names and the major value
     * @param regionMajorMap - Map of region names and their major value
     */

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void populateTable(HashMap regionMajorMap) {
        float spTextSize = 14;
        float textSize = spTextSize * getResources().getDisplayMetrics().scaledDensity;
        regionMajorMap.forEach((regionName, majorValue) -> {
            TableRow row = new TableRow(getActivity());
            row.setLayoutParams(new TableLayout.LayoutParams(TableLayout.LayoutParams.MATCH_PARENT, TableLayout.LayoutParams.WRAP_CONTENT));
            TextView regionField = new TextView(getActivity());
            TextView majorField = new TextView(getActivity());
            regionField.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            majorField.setLayoutParams(new TableRow.LayoutParams(TableRow.LayoutParams.WRAP_CONTENT, TableRow.LayoutParams.WRAP_CONTENT));
            regionField.setGravity(Gravity.CENTER);
            regionField.setTextSize(textSize);
            regionField.setText(regionName.toString());
            row.addView(regionField);
            majorField.setGravity(Gravity.CENTER);
            majorField.setTextSize(textSize);
            majorField.setText(majorValue.toString());
            row.addView(majorField);
            regionTable.addView(row);
        });
    }

    /**
     * Gets the current user's UUID from the database
     */
    private void getKontaktUUID() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users/" + user.getUid());

        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                kontaktUUID = dataSnapshot.child("uuid").getValue().toString();
            }

            @Override
            public void onCancelled(@NotNull DatabaseError databaseError) {
                Log.d("failure", "Unable to obtain user information");
            }
        });
    }

    /**
     * Default onCreateView function
     * @param inflater
     * @param container
     * @param savedInstanceState
     */
    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_beacon_region_list, container, false);
        Intent scanServiceIntent = new Intent(getActivity(), BeaconScanService.class);

        Button startScanButton = view.findViewById(R.id.start_button);
        startScanButton.setOnClickListener(v -> startBeaconScanService(scanServiceIntent));

        Button stopScanButton = view.findViewById(R.id.stop_button);
        stopScanButton.setOnClickListener(v -> stopBeaconScanService(scanServiceIntent));

        regionTable = (TableLayout) view.findViewById(R.id.regionTable);
        // Once the firebase query is completed the table is populated and buttons are enabled
        getUsersRegions(new OnQueryCompleteListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> newRegions = dataSnapshot.getChildren();
                for (DataSnapshot ds : newRegions) {
                    regionName = ds.getKey();
                    if (regionName != null) {
                        regionMajorValue = Integer.parseUnsignedInt(dataSnapshot.child(regionName).child("beaconMajor").getValue().toString());
                        regionMajorMap.put(regionName, regionMajorValue);
                    }
                }
                populateTable(regionMajorMap);
                startScanButton.setEnabled(true);
                stopScanButton.setEnabled(true);
            }

            @Override
            public void onStart() {
                Log.d("ONSTART", "Started");
            }

            @Override
            public void onFailure() {
                Log.d("failure", "Unable to obtain user information");
            }
        });
        return view;
    }

    /**
     * Queries the database to get currently setup rooms under the user
     * @param listener
     */
    public void getUsersRegions(final OnQueryCompleteListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users/" + user.getUid() + "/rooms");
        listener.onStart();

        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                listener.onSuccess(dataSnapshot);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onFailure();
            }
        });
    }

    /**
     * Sets up all the beacon regions for the user when the start scanning button is pressed
     * @param regionMajorMap
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setupBeaconRegions(HashMap regionMajorMap) {
        regionMajorMap.forEach((regionName, majorValue) -> {
            IBeaconRegion region = new BeaconRegion.Builder()
                    .identifier(regionName.toString())
                    .proximity(UUID.fromString(kontaktUUID))
                    .major((Integer) majorValue).build();
            beaconRegions.add(region);
            regionRssiMap.put(regionName.toString(), new HashMap<String, Double>());
            regionRssiMap.get(regionName).put("sum", 0.0);
            regionRssiMap.get(regionName).put("count", 0.0);
            regionRssiMap.get(regionName).put("dist", 0.0);
        });
    }

    /**
     * Starts the scanning when the region have been setup on the beacon end
     * @param scanServiceIntent
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void startBeaconScanService(Intent scanServiceIntent) {
        setupBeaconRegions(regionMajorMap);
        getActivity().startService(scanServiceIntent);
    }

    /**
     * Stops scanning for beacons
     * @param scanServiceIntent
     */
    public void stopBeaconScanService(Intent scanServiceIntent) {
        getActivity().stopService(scanServiceIntent);
    }

    // Interface to see when the firebase query is complete
    public interface OnQueryCompleteListener {
        void onSuccess(DataSnapshot dataSnapshot);

        void onStart();

        void onFailure();
    }
}

