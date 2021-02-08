package com.example.caregiver;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
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

    private void approveRequest(Object request){
        String userid = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("userId", "");
        final DatabaseReference ref = database.child("/users/" + userid);
        Map<String, Object> primary_caregiver = new HashMap<>();
        // add request person to primary caregiver if it doesn't exist yet
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                // if primary caregiver does not exist yet
                if(!snapshot.child("/primary_caregiver/").exists()){
                    primary_caregiver.put("/primary_caregiver/", request);
                    ref.updateChildren(primary_caregiver);
                    Log.d("SUCCESS", "added primary caregiver: " + request.toString());
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @SuppressLint("SetTextI18n")
    public void displayRequestList(View view, List allRequests, DataSnapshot snapshot){
        final ListView list = view.findViewById(R.id.requestList);
        ArrayAdapter<String> arrayAdapter;
        if(allRequests.isEmpty()){
            TextView text = view.findViewById(R.id.profileTextLabel);
            text.setText("Add a new Caregivee by clicking the round button below.");
            text.setTextSize(20);
        }
        arrayAdapter = new ArrayAdapter<String>(getActivity(),
                android.R.layout.simple_list_item_1, allRequests);

        list.setAdapter(arrayAdapter);
        Log.d("SUCCESS", "allRequests: " + allRequests.toString());
        list.setOnItemClickListener((parent, view1, position, id) -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setMessage("Approve " + allRequests.get(position).toString() + " as your Caregiver?")
                    .setPositiveButton("Approve", (dialog, which) -> {
                        // Add approved caregiver to Firebase - confirm user is a caregivee
                        if(PreferenceManager.getDefaultSharedPreferences(getActivity())
                                .getString("tag", "")
                                .equals("caregivee")){
                            for (DataSnapshot postSnapshot : snapshot.getChildren()){
                               if(postSnapshot.getValue().toString().equals(allRequests.get(position))){
                                   approveRequest(postSnapshot.getKey());
                               }
                            }
                        }
                    }).setNegativeButton("Cancel", null);
            AlertDialog alert = builder.create();
            alert.show();
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_request, container, false);

        List<String> allRequests = new ArrayList<>();

        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("userId", "");
        DatabaseReference ref = database.child("users/" + userId + "/requests/");
        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    allRequests.add(Objects.requireNonNull(postSnapshot.getValue()).toString());
                }
                displayRequestList(view, allRequests, snapshot);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.w("FAIL", "getRequests:onCancelled", databaseError.toException());
            }
        };
        ref.addValueEventListener(valueEventListener);

        Button plusButton = view.findViewById(R.id.plusButton);
        String role = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("tag", "");
        Log.d("SUCCESS", "role = " + role);
        if(role.equals("caregiver")) {
            plusButton.setVisibility(View.VISIBLE);
            plusButton.setOnClickListener(v -> {
                Intent intent = new Intent(view.getContext(), Request.class);
                startActivity(intent);
            });
        }

        return view;
    }
}