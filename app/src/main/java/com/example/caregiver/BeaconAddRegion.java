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

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BeaconAddRegion#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BeaconAddRegion extends Fragment {

    // Variables pointing to field names
    public EditText UUIDField;
    public EditText regionNameField;
    public EditText majorField;

    // Variables pointing to the user
    public String currentUUID;

    public BeaconAddRegion() {
        // Required empty public constructor
    }

    // TODO: Rename and change types and number of parameters
    public static BeaconAddRegion newInstance() {
        BeaconAddRegion fragment = new BeaconAddRegion();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
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
        Button updateButton = (Button) rootView.findViewById(R.id.uuidUpdateButton);
        updateButton.setOnClickListener(v -> updateUuid(user, rootView));

        Button addRegionButton = (Button) rootView.findViewById(R.id.add_region);
        addRegionButton.setOnClickListener(v -> addRegion(user, rootView));

        return rootView;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public void addRegion(@NonNull FirebaseUser user, View rootView) {

        UUIDField = (EditText) rootView.findViewById(R.id.UUIDText);
        String UUIDValue = UUIDField.getText().toString();

        regionNameField = (EditText) rootView.findViewById(R.id.regionName);
        String regionName = regionNameField.getText().toString();

        majorField = (EditText) rootView.findViewById(R.id.major);
        String majorValue = majorField.getText().toString();

        if (UUIDValue.isEmpty() || regionName.isEmpty() || majorValue.isEmpty()) {
            displayErrorMessage("One or more fields are empty.", rootView);
        } else {
            displayErrorMessage("", rootView);
            regionInfo newRegionInfo = new regionInfo(UUIDValue, regionName, majorValue);
            updateRegionInfoInBackend(user, newRegionInfo);
            Log.i("Sample", "region Info = " + newRegionInfo.toString());
        }

    }

//    public void startBeaconScanService(Intent scanServiceIntent) {
//        getActivity().startService(scanServiceIntent);
//    }
//
//    public void stopBeaconScanService(Intent scanServiceIntent) {
//        getActivity().stopService(scanServiceIntent);
//    }

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

    public void updateUuid(@NonNull FirebaseUser user, View rootView) {
        UUIDField = (EditText) rootView.findViewById(R.id.UUIDText);
        String UUIDValue = UUIDField.getText().toString();
//        if (UUIDField.getText() != null) {
//            String UUIDValue = UUIDField.getText().toString();
        if (UUIDValue != null && !UUIDValue.isEmpty()) {
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("users").child(user.getUid()).child("uuid").setValue(UUIDValue);
            currentUUID = UUIDValue;
            UUIDField.setHint(currentUUID);
        }
//        }
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
                    UUIDField = (EditText) view.findViewById(R.id.UUIDText);
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