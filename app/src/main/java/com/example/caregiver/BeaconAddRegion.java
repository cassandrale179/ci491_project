package com.example.caregiver;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import static com.example.caregiver.BeaconRegionList.scanServiceIntent;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BeaconAddRegion#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeaconAddRegion extends Fragment {

    public static String regionName;
    public static String regionMajorValue;
    // Variables pointing to field names
    public EditText UUIDField;
    public EditText regionNameField;
    public EditText majorField;
    // Variables pointing to the user
    public String currentUUID;

    public BeaconAddRegion() {
        // Required empty public constructor
    }

    public static BeaconAddRegion newInstance() {
        BeaconAddRegion fragment = new BeaconAddRegion();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        BeaconRegionList.getInstance().getKontaktUUID();
        super.onCreate(savedInstanceState);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Get current userId
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = preferences.getString("userId", "");
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_beacon_add_region, container, false);

        displayUuid(rootView, userId);
        Button updateButton = rootView.findViewById(R.id.uuidUpdateButton);
        updateButton.setOnClickListener(v -> updateUuid(user, rootView));

        Button addRegionButton = rootView.findViewById(R.id.add_region);
        addRegionButton.setOnClickListener(v -> addRegion(user, rootView));

        return rootView;
    }

    public String getUUIDValue(View rootView) {
        UUIDField = rootView.findViewById(R.id.UUIDText);
        return UUIDField.getText().toString();
    }

    public String getRegionName(View rootView) {
        regionNameField = rootView.findViewById(R.id.regionName);
        return regionNameField.getText().toString();
    }

    public String getMajorValue(View rootView) {
        majorField = rootView.findViewById(R.id.major);
        return majorField.getText().toString();
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addRegion(@NonNull FirebaseUser user, View rootView) {
        BeaconRegionList.getInstance().getUsersRegions(new BeaconRegionList.OnQueryCompleteListener() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                BeaconRegionList.getInstance().createRegionMajorMap(dataSnapshot);
                Log.i("sample", "regionMajorMap after the getUsersRegion call" + BeaconRegionList.regionMajorMap.toString());
                String UUIDValue = getUUIDValue(rootView);

                String regionName = getRegionName(rootView);

                String majorValue = getMajorValue(rootView);

                if (regionName.isEmpty() || majorValue.isEmpty()) {
                    displayErrorMessage("One or more fields are empty.", rootView);
                }
                // check if region/major value has already been used
                else if (BeaconRegionList.regionMajorMap.containsKey(regionName.toLowerCase()) || BeaconRegionList.regionMajorMap.containsValue(majorValue)) {
                    displayErrorMessage("This region or major value has already been used.", rootView);
                } else {
                    displayErrorMessage("", rootView);
                    regionInfo newRegionInfo = new regionInfo(UUIDValue, regionName, majorValue);
                    updateRegionInfoInBackend(user, newRegionInfo);
                    // call stop scanning using scanServiceIntent used to start it
                    if (BeaconRegionList.isAlreadyScanning)
                        BeaconRegionList.getInstance().stopBeaconScanService(scanServiceIntent);
                    // add region and major to the regionMajorMap
                    updateRegionMajorMap(regionName, majorValue);
                    // call setupSpaces() using updated map
                    BeaconRegionList.getInstance().setupBeaconRegions(BeaconRegionList.regionMajorMap);
                    // call start scanning using same scanService intent used to stop it
                    BeaconRegionList.getInstance().startBeaconScanService(scanServiceIntent);
                }
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
    }

    public void updateRegionMajorMap(String regionName, String majorValue) {
        BeaconRegionList.regionMajorMap.put(regionName.toLowerCase(), majorValue);
    }

    public void updateRegionInfoInBackend(@NonNull FirebaseUser user, regionInfo newRegionInfo) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        HashMap<String, Object> newRegion = new HashMap<String, Object>();
        HashMap<String, Object> beaconMajor = new HashMap<String, Object>();
        beaconMajor.put("beaconMajor", newRegionInfo.majorValue);
        newRegion.put(newRegionInfo.regionName, beaconMajor);
        database.child("users").child(user.getUid()).child("rooms").updateChildren(newRegion);
    }

    public void displayErrorMessage(String sourceString, View rootView) {
        TextView textView = rootView.findViewById(R.id.addRegionMessage);
        textView.setText(Html.fromHtml(sourceString));
        textView.setVisibility(View.VISIBLE);
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void updateUuid(@NonNull FirebaseUser user, View rootView) {
        String UUIDValue = getUUIDValue(rootView);
        if (UUIDValue != null && !UUIDValue.isEmpty()) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("users").child(user.getUid()).child("uuid").setValue(UUIDValue);
            currentUUID = UUIDValue;
            UUIDField.setHint(currentUUID);
        }
        if (BeaconRegionList.isAlreadyScanning)
            BeaconRegionList.getInstance().stopBeaconScanService(scanServiceIntent);
        // call setupSpaces() using updated map
        BeaconRegionList.getInstance().setupBeaconRegions(BeaconRegionList.regionMajorMap, UUIDValue);
        // call start scanning using same scanService intent used to stop it
        BeaconRegionList.getInstance().startBeaconScanService(scanServiceIntent);
    }

    public void displayUuid(View view, String userId) {
        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference ref = database.child("users/" + userId);

        // Attach a listener to read data of user (name, email, id)
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child("uuid").getValue() != null) {
                    currentUUID = dataSnapshot.child("uuid").getValue().toString();
                    UUIDField = view.findViewById(R.id.UUIDText);
                    UUIDField.setHint(currentUUID);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d("failure", "Unable to obtain user information");
            }
        });

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