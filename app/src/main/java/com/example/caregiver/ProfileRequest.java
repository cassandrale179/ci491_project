package com.example.caregiver;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ProfileRequest#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ProfileRequest extends Fragment {

    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();


    public ProfileRequest() {
        // Required empty public constructor
    }

    public static ProfileRequest newInstance(String param1, String param2) {
        ProfileRequest fragment = new ProfileRequest();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void declineRequest(DataSnapshot requestSnapshot, List allRequests, int position){
        // get user info & db ref
        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("userId", "");
        final DatabaseReference ref = database.child("/users/" + userId);

        // find the corresponding request to remove
        for (DataSnapshot postSnapshot : requestSnapshot.getChildren()){
            if(postSnapshot.getValue().toString().equals(allRequests.get(position))){
                ref.child("/requests/" + postSnapshot.getKey()).removeValue();
                Log.i("INFO", "declineRequest:removed " + postSnapshot.getValue());
            }
        }
    }

    private void approveRequest(DataSnapshot requestSnapshot){
        // get user info & db ref
        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("userId", "");
        String name = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("name", "");
        final DatabaseReference ref = database.child("/users/" + userId);

        Map<String, Object> caregivers = new HashMap<>();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.KITKAT)
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean caregiverExists = false;
                // if caregivers list exists, check if requested caregiver exists there
                if(snapshot.child("/caregivers/").exists()) {
                    for (DataSnapshot child : snapshot.child("/caregivers/").getChildren()) {
                        if (Objects.equals(child.getKey(), requestSnapshot.getKey())) {
                            caregiverExists = true; // this user already exists as a caregiver
                        }
                    }
                }
                // if caregiver doesn't exist in list of caregivers ->
                // add caregiver to caregivee's list
                // add caregivee to caregiver's list
                if(!caregiverExists){
                    caregivers.put("/caregivers/" + requestSnapshot.getKey(), requestSnapshot.getValue());
                    ref.updateChildren(caregivers);
                    Log.i("SUCCESS", "approveRequest:added caregiver " + requestSnapshot.getValue() + " to caregivee " + name);

                    // add caregivee to caregiver's list
                    final DatabaseReference caregiveeRef = database.child("/users/" + requestSnapshot.getKey());
                    Map<String, Object> caregivers = new HashMap<>();
                    caregivers.put("/caregivees/" + userId, name);
                    caregiveeRef.updateChildren(caregivers);
                    Log.i("INFO", "approveRequest:added caregivee " + name + " to caregiver " + requestSnapshot.getValue());

                } else {
                    // inform user caregiver already exists
                    AlertDialog.Builder builder = new AlertDialog.Builder(Objects.requireNonNull(getActivity()))
                            .setMessage(requestSnapshot.getValue() + " is already your caregiver.")
                            .setPositiveButton("Okay", null);
                    builder.create().show();
                }
                // remove request
                if(snapshot.child("/requests/").exists()){
                    ref.child("/requests/" + requestSnapshot.getKey()).removeValue();
                    Log.i("INFO", "approveRequest:removed request from " + requestSnapshot.getValue());
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FAIL", "approveRequest:onCancelled " + error.toException());
            }
        });
    }

    private void displayMessageCaregiver(View view){
        // display option to send request
        TextView text = view.findViewById(R.id.profileTextLabel);
        text.setText(R.string.add_caregivee_msg);

        Button plusButton = view.findViewById(R.id.plusButton);
        // show request initiation button
        plusButton.setVisibility(View.VISIBLE);
        plusButton.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), Request.class);
            startActivity(intent);
        });
    }

    @SuppressLint("SetTextI18n")
    private void displayRequestListCaregivee(View view, List allRequests, DataSnapshot snapshot){
        // get list element
        final ListView list = view.findViewById(R.id.requestList);
        list.setAdapter(null); // clear previous contents
        ArrayAdapter<String> arrayAdapter;

        // if requests is empty
        if(allRequests.isEmpty()){
            TextView text = view.findViewById(R.id.profileTextLabel);
            text.setText("No more requests. You're all clear :)");
        } else {
            arrayAdapter = new ArrayAdapter<String>(getActivity(),
                    android.R.layout.simple_list_item_1, allRequests);

            list.setAdapter(arrayAdapter);
            list.setOnItemClickListener((parent, view1, position, id) -> {
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Approve " + allRequests.get(position).toString() + " as your Caregiver?")
                        .setPositiveButton("Approve", (dialog, which) -> {
                            // Add approved caregiver to Firebase
                            for (DataSnapshot postSnapshot : snapshot.getChildren()){
                                if(postSnapshot.getValue().toString().equals(allRequests.get(position))){
                                    approveRequest(postSnapshot);
                                }
                            }
                            // display success message
                            Log.i("SUCCESS", "displayRequestListCaregivee: successful request approval");

                        }).setNegativeButton("Decline", ((dialog, which) -> {
                            // Decline request & remove from Firebase
                            declineRequest(snapshot, allRequests, position);
                        }));
                builder.create().show();
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_request, container, false);

        List<String> allRequests = new ArrayList<>();

        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("userId", "");
        String role = PreferenceManager.getDefaultSharedPreferences(getActivity())
                .getString("tag", "");
        boolean isCaregivee = role.equals("caregivee");

        DatabaseReference ref = database.child("users/" + userId + "/requests/");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if caregivee, formulate list with requests & display
                if(isCaregivee) {
                    allRequests.clear();
                    for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                        allRequests.add(Objects.requireNonNull(postSnapshot.getValue()).toString());
                    }
                    displayRequestListCaregivee(view, allRequests, snapshot);
                } else {
                    // is caregiver, display message through component
                    displayMessageCaregiver(view);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("FAIL", "getRequests:onCancelled", databaseError.toException());
            }
        };
        ref.addValueEventListener(valueEventListener);

        return view;
    }
}