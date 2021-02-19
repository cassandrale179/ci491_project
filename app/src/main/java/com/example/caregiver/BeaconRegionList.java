package com.example.caregiver;

import android.content.Intent;
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
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BeaconRegionList#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeaconRegionList extends Fragment {

    public static final HashMap<String, HashMap<String, Double>> regionRssiMap = new HashMap<>();
    public static String regionName;
    public static int regionMajorValue;
    public static String kontaktUUID;
    public static Collection<IBeaconRegion> beaconRegions = new ArrayList<>();
    public TableLayout regionTable;


    public BeaconRegionList() {
        // Required empty public constructor
    }

    public static BeaconRegionList newInstance() {
        BeaconRegionList fragment = new BeaconRegionList();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getKontaktUUID();
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private void populateTable(HashMap regionMajorMap) {
        float spTextSize = 16;
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

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_beacon_region_list, container, false);
        Intent scanServiceIntent = new Intent(getActivity(), BeaconScanService.class);

        Button startScanButton = view.findViewById(R.id.start_button);
        // Once beacon regions are extracted from firebase database, BeaconScanService is started
        startScanButton.setOnClickListener(v -> getBeaconRegions(scanServiceIntent));

        Button stopScanButton = view.findViewById(R.id.stop_button);
        stopScanButton.setOnClickListener(v -> stopBeaconScanService(scanServiceIntent));

        regionTable = (TableLayout) view.findViewById(R.id.regionTable);

        return view;
    }

    private void getBeaconRegions(Intent scanServiceIntent) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users/" + user.getUid() + "/rooms");
        HashMap<String, Integer> regionMajorMap = new HashMap<String, Integer>();

        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.O)
            @Override
            public void onDataChange(@NotNull DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> newRegions = dataSnapshot.getChildren();

                for (DataSnapshot ds : newRegions) {
                    regionName = ds.getKey();
                    Log.i("Sample", "regionName = " + regionName);
                    if (regionName != null) {
                        regionMajorValue = Integer.parseUnsignedInt(dataSnapshot.child(regionName).child("beaconMajor").getValue().toString());
                        IBeaconRegion region = new BeaconRegion.Builder()
                                .identifier(regionName)
                                .proximity(UUID.fromString(kontaktUUID))
                                .major(regionMajorValue).build();

                        beaconRegions.add(region);
                        // TODO: Add region and major to a map
                        regionMajorMap.put(regionName, regionMajorValue);
                        regionRssiMap.put(regionName, new HashMap<String, Double>());
                        regionRssiMap.get(regionName).put("sum", 0.0);
                        regionRssiMap.get(regionName).put("count", 0.0);
                        regionRssiMap.get(regionName).put("dist", 0.0);
                    }
                }
                // All beacon regions are available from firebase, starting BeaconScanService
                startBeaconScanService(scanServiceIntent);
                // TODO: Call populate table function
                populateTable(regionMajorMap);
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
}

