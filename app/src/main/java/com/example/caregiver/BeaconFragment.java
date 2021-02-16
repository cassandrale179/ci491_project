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
import java.util.UUID;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
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


    ViewPager viewPager;
    TabLayout tabLayout;


    public BeaconFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     * @return A new instance of fragment ProfileFragment.
     */
    public static BeaconFragment newInstance() {
        BeaconFragment fragment = new BeaconFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        getKontaktUUID();
        super.onCreate(savedInstanceState);
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

        View view =  inflater.inflate(R.layout.fragment_beacon, container, false);

        tabLayout = (TabLayout) view.findViewById(R.id.tab_layout);
        viewPager = (ViewPager) view.findViewById(R.id.view_pager);
        prepareViewPager(viewPager);
        tabLayout.setupWithViewPager(viewPager);
        return view;
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

                for (DataSnapshot ds : newRegions) {
                    regionName = ds.getKey();
                    regionMajorValue = Integer.parseUnsignedInt(dataSnapshot.child(regionName).child("beaconMajor").getValue().toString());
                    IBeaconRegion region = new BeaconRegion.Builder()
                            .identifier(regionName)
                            .proximity(UUID.fromString(kontaktUUID))
                            .major(regionMajorValue).build();

                    beaconRegions.add(region);

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

    private void prepareViewPager(ViewPager viewPager){
        BeaconAdapter adapter = new BeaconAdapter(getFragmentManager());
        viewPager.setAdapter(adapter);
    }

    private class BeaconAdapter extends FragmentPagerAdapter{
        public BeaconAdapter(@NonNull FragmentManager fm){
            super(fm);

        }

        @NonNull
        @Override
        public Fragment getItem(int position) {
            switch (position) {
                case 0:
                    BeaconRegionList regionListFragment = new BeaconRegionList();
                    return regionListFragment;
                case 1:
                    BeaconAddRegion addRegionFragment = new BeaconAddRegion();
                    return addRegionFragment;
                default:
                    return null;
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

        @Override
        public int getCount() {
            return 2;
        }

        @Nullable
        @Override
        public CharSequence getPageTitle(int position){
            switch (position) {
                case 0:
                    return "Regions";
                case 1:
                    return "Add Region";
                default:
                    return null;
            }
        }
    }

}

//import android.content.ContentProviderClient;
//import android.content.Intent;
//import android.content.SharedPreferences;
//import android.os.Build;
//import android.os.Bundle;
//
//import androidx.annotation.NonNull;
//import androidx.annotation.RequiresApi;
//import androidx.fragment.app.Fragment;
//
//import android.preference.PreferenceManager;
//import android.text.Html;
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//import android.widget.Button;
//import android.widget.EditText;
//import android.widget.TextView;
//
//import com.google.firebase.auth.AuthCredential;
//import com.google.firebase.auth.EmailAuthProvider;
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.auth.FirebaseUser;
//import com.google.firebase.database.DataSnapshot;
//import com.google.firebase.database.DatabaseError;
//import com.google.firebase.database.DatabaseReference;
//import com.google.firebase.database.FirebaseDatabase;
//import com.google.firebase.database.ValueEventListener;
//
//import com.example.caregiver.services.BeaconScanService;
//
//import java.util.HashMap;
//
//public class BeaconFragment extends Fragment {
//
//    // Variables pointing to field names
//    public EditText UUIDField; //check
//    public EditText regionNameField;
//    public EditText majorField;
//    public EditText minorField;
//
//    // Variables pointing to the user
//    public String currentUUID;
//
//    public static class regionInfo {
//        public String UUID;
//        public String regionName;
//        public String majorValue;
//
//        public regionInfo(String UUID, String regionName, String majorValue) {
//            this.UUID = UUID;
//            this.regionName = regionName;
//            this.majorValue = majorValue;
//        }
//
//        @Override
//        public String toString() {
//            return "regionInfo{" +
//                    "UUID='" + UUID + '\'' +
//                    ", regionName='" + regionName + '\'' +
//                    ", majorValue=" + majorValue +
//                    '}';
//        }
//    }
//
//    public BeaconFragment() {
//        // Required empty public constructor
//    }
//
//    public static BeaconFragment newInstance() {
//        return new BeaconFragment();
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        // Get current userId
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        String userId = preferences.getString("userId", "");
//        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
//
//        // Inflate the layout for this fragment
//        View rootView = inflater.inflate(R.layout.fragment_beacon, container, false);
//        Intent scanServiceIntent = new Intent(getActivity(), BeaconScanService.class);
//
//        displayUuid(rootView, userId);
//        Button updateButton = (Button) rootView.findViewById(R.id.uuidUpdateButton);
//        updateButton.setOnClickListener(v -> updateUuid(user, rootView));
//
//        Button startScanButton = rootView.findViewById(R.id.start_button);
//        startScanButton.setOnClickListener(v -> startBeaconScanService(scanServiceIntent));
//
//        Button stopScanButton = rootView.findViewById(R.id.stop_button);
//        stopScanButton.setOnClickListener(v -> stopBeaconScanService(scanServiceIntent));
//
//        Button addRegionButton = rootView.findViewById(R.id.add_region);
//        addRegionButton.setOnClickListener(v -> addRegion(user, rootView));
//
//        return rootView;
//
//    }
//
//    public void startBeaconScanService(Intent scanServiceIntent) {
//        getActivity().startService(scanServiceIntent);
//    }
//
//    public void stopBeaconScanService(Intent scanServiceIntent) {
//        getActivity().stopService(scanServiceIntent);
//    }
//
//    @RequiresApi(api = Build.VERSION_CODES.O)
//    public void addRegion(@NonNull FirebaseUser user, View rootView) {
//
//        EditText UUIDField = (EditText) rootView.findViewById(R.id.UUIDText);
//        String UUIDValue = UUIDField.getText().toString();
//
//        EditText regionNameField = (EditText) rootView.findViewById(R.id.regionName);
//        String regionName = regionNameField.getText().toString();
//
//        EditText majorField = (EditText) rootView.findViewById(R.id.major);
//        String majorValue = majorField.getText().toString();
//
//        if (UUIDValue.isEmpty() || regionName.isEmpty() || majorValue.isEmpty()) {
//            displayErrorMessage("One or more fields are empty.", rootView);
//        } else {
//            displayErrorMessage("", rootView);
//            regionInfo newRegionInfo = new regionInfo(UUIDValue, regionName, majorValue);
//            updateRegionInfoInBackend(user, newRegionInfo);
//            Log.i("Sample", "region Info = " + newRegionInfo.toString());
//        }
//
//    }
//
//    public void updateRegionInfoInBackend(@NonNull FirebaseUser user, regionInfo newRegionInfo) {
//        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//        HashMap<String, Object> newRegion = new HashMap<String, Object>();
//        newRegion.put(newRegionInfo.regionName, newRegionInfo.majorValue);
//        database.child("users").child(user.getUid()).child("rooms").updateChildren(newRegion);
//    }
//
//    public void displayErrorMessage(String sourceString, View rootView) {
//        TextView textView = rootView.findViewById(R.id.addRegionMessage);
//        textView.setText(Html.fromHtml(sourceString));
//        textView.setVisibility(View.VISIBLE);
//    }
//
//    public void updateUuid(@NonNull FirebaseUser user, View rootView) {
//        EditText UUIDField = (EditText) rootView.findViewById(R.id.UUIDText);
//        String UUIDValue = UUIDField.getText().toString();
////        if (UUIDField.getText() != null) {
////            String UUIDValue = UUIDField.getText().toString();
//            if (UUIDValue != null && !UUIDValue.isEmpty()) {
//                DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
//                rootRef.child("users").child(user.getUid()).child("uuid").setValue(UUIDValue);
//                currentUUID = UUIDValue;
//                UUIDField.setHint(currentUUID);
//            }
////        }
//    }
//
//    public void displayUuid(View view, String userId) {
//        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
//        DatabaseReference ref = database.child("users/" + userId);
//
//        // Attach a listener to read data of user (name, email, id)
//        ref.addValueEventListener(new ValueEventListener() {
//            @Override
//            public void onDataChange(DataSnapshot dataSnapshot) {
//                if (dataSnapshot.child("uuid").getValue() != null) {
//                    currentUUID = dataSnapshot.child("uuid").getValue().toString();
//                    EditText UUIDField = (EditText) view.findViewById(R.id.UUIDText);
//                    UUIDField.setHint(currentUUID);
//                }
//            }
//
//            @Override
//            public void onCancelled(DatabaseError databaseError) {
//                Log.d("failure", "Unable to obtain user information");
//            }
//        });
//
//    }
//
//
//}

