package com.example.caregiver;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.renderscript.Sampler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HomeCaregivee extends Fragment {

    // Global reference to Firebase
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    public HomeCaregivee() { }

    public static HomeCaregivee newInstance() {
        HomeCaregivee fragment = new HomeCaregivee();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home_caregivee, container, false);

        // Get the list view
        final ListView list = view.findViewById(R.id.caregiverHomeList);

        // Generate caregivee names
        ArrayList<String> caregiverNames = new ArrayList<>();
        ArrayList<String> caregiverIds = new ArrayList<>();

        // Get the user caregivers
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = preferences.getString("userId", "");
        DatabaseReference userRef = database.child("users/" + userId);

        userRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot caregivee :  snapshot.child("caregivers").getChildren()) {
                    String caregiverId = caregivee.getKey();
                    String caregiverName = caregivee.getValue().toString();
                    caregiverNames.add(caregiverName);
                    caregiverIds.add(caregiverId);
                }
                ArrayAdapter<String> arrayAdapter = new ArrayAdapter<String>(
                        getContext(),android.R.layout.simple_list_item_1, caregiverNames);
                list.setAdapter(arrayAdapter);

                // Set a listener when people click on item
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        String caregiverId = caregiverIds.get(position);
                        if (caregiverId != null){
                            displayCaregiver(caregiverId);
                        }
                        Log.d("error", "Can't find caregiver id.");
                    }
                });
            }@Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("error", "Can't query caregivers for this caregivee");
            }
        });
        return view;
    }

    protected void displayCaregiver(String caregiverId){
        DatabaseReference caregiverRef = database.child("users/" + caregiverId);
        caregiverRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

            }@Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("error", "Can't display caregiver profile");
            }
        });

    }
}