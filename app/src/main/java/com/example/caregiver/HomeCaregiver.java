package com.example.caregiver;

import android.content.Intent;
import android.content.SharedPreferences;
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
import android.widget.ExpandableListView;

import com.example.caregiver.model.Task;
import com.example.caregiver.model.User;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link HomeCaregiver#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeCaregiver extends Fragment {

    // Set global variables
    ExpandableListView caregiveeList;
    MainAdapter adapter;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    ArrayList<User> caregivees = new ArrayList<>();
    DatabaseReference userRef;
    String userId;


    public HomeCaregiver() { }

    public static HomeCaregiver newInstance() {
        HomeCaregiver fragment = new HomeCaregiver();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Return list of caregivees associated with the caregiver.
     */
    public void queryCaregivees() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        userId = preferences.getString("userId", "");
        userRef = database.child("users/" + userId);
        userRef.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot caregivee :  snapshot.child("caregivees").getChildren()) {
                String caregiveeId = caregivee.getKey();
                String caregiveeName = caregivee.getValue().toString();
                User user = new User(caregiveeId, caregiveeName);
                caregivees.add(user);
            }

            displayCaregiveeList();
        }@Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("error", "Can't query caregivees for this caregiver");
        }
        });
    }

    /**
     * Call after we query all caregivee and display functionality associated with them.
     */
    @RequiresApi(api = Build.VERSION_CODES.N)
    public void displayCaregiveeList(){
        ArrayList<String> caregiveeNames = new ArrayList<>();
        HashMap<String, ArrayList<String>> listChild = new HashMap<>();

        // Sort caregivee list by name
        Collections.sort(caregivees, (User a, User b) -> a.name.compareToIgnoreCase(b.name));
        caregivees.forEach(caregivee -> {
            ArrayList<String> listChildValues = new ArrayList<String>(
                    Arrays.asList("View Profile", "Set Tasks", "See Progress", "Delete Caregivee"));
            caregiveeNames.add(caregivee.name);
            listChild.put(caregivee.name, listChildValues);
        });
        adapter = new MainAdapter(caregiveeNames, listChild);
        caregiveeList.setAdapter(adapter);
        setOnChildListener();
    }

    /**
     * Helper function to view the caregivee profile
     * @param groupPosition index of the caregivee in the list
     */
    public void viewCaregiveeProfile(int groupPosition){
        String caregiveeName = caregivees.get(groupPosition).name;
        String caregiveeId = caregivees.get(groupPosition).id;
        DatabaseReference ref = database.child("users/" + caregiveeId);
        ProfileInfo fragment = new ProfileInfo();
        Bundle args = new Bundle();


        ref.addValueEventListener(new ValueEventListener() {@Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String caregiveeEmail = snapshot.child("email").getValue().toString();
            if (snapshot.child("notes").getValue() != null){
                String caregiveeNotes = snapshot.child("notes").getValue().toString();
                args.putString("otherNotes", caregiveeNotes);
            }
            args.putString("otherEmail", caregiveeEmail);
            args.putString("otherName", caregiveeName);
            fragment.setArguments(args);
            ((Dashboard)getActivity()).replaceActiveFragment(fragment);
        }@Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("error", "Can't query caregivees for this caregiver");
        }
        });
    }

    /**
     * Function to remove a caregivee under a caregiver's care
     * @param groupPosition index of the caregivee in the list
     */
    public void removeCaregiveePopUp(int groupPosition){
        DatabaseReference ref =  database.child("users");
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage("Want to remove " + caregivees.get(groupPosition).name + " from your care?")
                .setPositiveButton("Yes", (dialog, which) ->{
                    String caregiveeId = caregivees.get(groupPosition).id;
                    userRef.child("caregivees").child(caregiveeId).removeValue();
                    ref.child(caregiveeId).child("caregivers").child(userId).removeValue();
                    startActivity(new Intent(getContext(), Dashboard.class));
                }).setNegativeButton("No", null);
        builder.create().show();
    }

    public void setOnChildListener(){
        caregiveeList.setOnChildClickListener((parent, view, groupPosition, childPosition, id) -> {
            switch(childPosition) {
                case 0:
                    viewCaregiveeProfile(groupPosition);
                    break;
                case 1:
                    ((Dashboard)getActivity()).replaceActiveFragment(new SetTasksFragment(caregivees.get(groupPosition).id));
                    break;
                case 2:
                    Intent i = new Intent(getContext(), ViewProgress.class);
                    i.putExtra("caregiveeName", caregivees.get(groupPosition).name);
                    i.putExtra("caregiveeID", caregivees.get(groupPosition).id);
                    startActivity(i);
                    break;
                case 3:
                    removeCaregiveePopUp(groupPosition);
                    break;
            }
            return true;
        });
    }


    /**
     * Redirect caregiver to request page if they want to add more caregivee
     */
    private View.OnClickListener redirectToRequestPageListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(v.getContext(), Request.class);
            startActivity(i);
        }
    };


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_caregiver, container, false);

        // Get the caregivee list and display their information
        caregiveeList = (ExpandableListView) view.findViewById(R.id.caregiveeHomelist);

        // Call firebase to query the caregivees
        queryCaregivees();

        // Set the floating action button to redirect to match request page
        FloatingActionButton plusButton = view.findViewById(R.id.addCaregiveeButton);
        plusButton.setOnClickListener(redirectToRequestPageListener);

        return view;
    }
}