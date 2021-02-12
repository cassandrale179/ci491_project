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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
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

    /**
     * Function to decline user request
     * @param requestSnapshot snapshot represent list of requests
     * @param allRequests list to store requests
     * @param position position of the request
     */

    private void declineRequest(DataSnapshot requestSnapshot, List allRequests, int position) {
        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("userId", "");
        final DatabaseReference ref = database.child("/users/" + userId);
        for (DataSnapshot postSnapshot: requestSnapshot.getChildren()) {
            if (postSnapshot.getValue().toString().equals(allRequests.get(position))) {
                ref.child("/requests/" + postSnapshot.getKey()).removeValue();
                Log.i("INFO", "declineRequest:removed " + postSnapshot.getValue());
            }
        }
    }

    /**
     * Approve request.
     * @param requestSnapshot snapshot represent list of requests
     */
    private void approveRequest(DataSnapshot requestSnapshot) {
        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("userId", "");
        String name = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("name", "");
        final DatabaseReference ref = database.child("/users/" + userId);

        Map < String,
                Object > caregivers = new HashMap < >();
        ref.addListenerForSingleValueEvent(new ValueEventListener() {@RequiresApi(api = Build.VERSION_CODES.KITKAT)@Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            boolean caregiverExists = false;
            // If caregivers list exists, check if requested caregiver exists there.
            if (snapshot.child("/caregivers/").exists()) {
                for (DataSnapshot child: snapshot.child("/caregivers/").getChildren()) {
                    if (Objects.equals(child.getKey(), requestSnapshot.getKey())) {
                        caregiverExists = true; // this user already exists as a caregiver
                    }
                }
            }
            // If caregiver doesn't exist in list of caregivers, add them to caregivee list.
            if (!caregiverExists) {
                caregivers.put("/caregivers/" + requestSnapshot.getKey(), requestSnapshot.getValue());
                ref.updateChildren(caregivers);
                Log.i("SUCCESS", "approveRequest:added caregiver " + requestSnapshot.getValue() + " to caregivee " + name);

                final DatabaseReference caregiveeRef = database.child("/users/" + requestSnapshot.getKey());
                Map < String,
                        Object > caregivers = new HashMap < >();
                caregivers.put("/caregivees/" + userId, name);
                caregiveeRef.updateChildren(caregivers);
                Log.i("INFO", "approveRequest:added caregivee " + name + " to caregiver " + requestSnapshot.getValue());

            } else {
                // Inform user caregiver already exists.
                AlertDialog.Builder builder = new AlertDialog.Builder(
                        Objects.requireNonNull(getActivity()))
                        .setMessage(requestSnapshot.getValue() + " is already your caregiver.")
                        .setPositiveButton("Okay", null);
                builder.create().show();
            }
            // Remove request after approved.
            if (snapshot.child("/requests/").exists()) {
                ref.child("/requests/" + requestSnapshot.getKey()).removeValue();
                Log.i("INFO", "approveRequest:removed request from " + requestSnapshot.getValue());
            }
        }@Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.e("FAIL", "approveRequest:onCancelled " + error.toException());
        }
        });
    }

    /**
     * Function display option to send request for caregiver and show request initiation.
     * @param view The ProfileRequest view.
     */
    private void displayMessageCaregiver(View view) {
        // display option to send request
        TextView text = view.findViewById(R.id.profileTextLabel);
        text.setText(R.string.add_caregivee_msg);

        // Add in + button to send requests to caregivee
        FloatingActionButton plusButton = view.findViewById(R.id.plusButton);
        plusButton.setVisibility(View.VISIBLE);
        plusButton.setOnClickListener(v ->{
            Intent intent = new Intent(view.getContext(), Request.class);
            startActivity(intent);
        });
    }

    /**
     * Function to display request of caregivee onto the main screen
     * @param view The ProfileRequestView
     * @param allRequests the list of requests
     * @param snapshot
     */
    @SuppressLint("SetTextI18n")
    private void displayRequestListCaregivee(View view, List allRequests, DataSnapshot snapshot) {
        final ListView list = view.findViewById(R.id.requestList);
        list.setAdapter(null); // clear previous contents
        ArrayAdapter < String > arrayAdapter;

        if (allRequests.isEmpty()) {
            TextView text = view.findViewById(R.id.profileTextLabel);
            text.setText("No more requests. You're all clear :)");
        } else {
            arrayAdapter = new ArrayAdapter < String > (getActivity(), android.R.layout.simple_list_item_1, allRequests);

            list.setAdapter(arrayAdapter);
            list.setOnItemClickListener((parent, view1, position, id) ->{
                AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
                builder.setMessage("Approve " + allRequests.get(position).toString() + " as your Caregiver?")
                        .setPositiveButton("Approve", (dialog, which) ->{
                    // Add approved caregiver to Firebase
                    for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                        if (postSnapshot.getValue().toString().equals(allRequests.get(position))) {
                            approveRequest(postSnapshot);
                        }
                    }
                    Log.i("SUCCESS", "displayRequestListCaregivee: successful request approval");

                }).setNegativeButton("Decline", ((dialoButtong, which) ->{
                    declineRequest(snapshot, allRequests, position);
                }));
                builder.create().show();
            });
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profile_request, container, false);

        List < String > allRequests = new ArrayList < >();
        String userId = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("userId", "");
        String role = PreferenceManager.getDefaultSharedPreferences(getActivity()).getString("tag", "");

        DatabaseReference ref = database.child("users/" + userId + "/requests/");
        ValueEventListener valueEventListener = new ValueEventListener() {@Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            // If the user is caregivee, formulate list with requests & display.
            if (role.equals("caregivee")) {
                allRequests.clear();
                for (DataSnapshot postSnapshot: snapshot.getChildren()) {
                    allRequests.add(Objects.requireNonNull(postSnapshot.getValue()).toString());
                }
                displayRequestListCaregivee(view, allRequests, snapshot);
            }
            else {
                displayMessageCaregiver(view);
            }
        }@Override
        public void onCancelled(DatabaseError databaseError) {
            Log.e("FAIL", "getRequests:onCancelled", databaseError.toException());
        }
        };
        ref.addValueEventListener(valueEventListener);
        return view;
    }
}