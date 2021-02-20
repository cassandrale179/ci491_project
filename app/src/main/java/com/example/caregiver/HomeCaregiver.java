package com.example.caregiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
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
    ArrayList<String> caregiveeNames  = new ArrayList<>();
    ArrayList<String> caregiveeIds = new ArrayList<>();

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
        String userId = preferences.getString("userId", "");
        DatabaseReference ref = database.child("users/" + userId);

        ref.addValueEventListener(new ValueEventListener() {@Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            for (DataSnapshot caregivee :  snapshot.child("caregivees").getChildren()) {
                String caregiveeId = caregivee.getKey();
                String caregiveeName = caregivee.getValue().toString();
                caregiveeNames.add(caregiveeName);
                caregiveeIds.add(caregiveeId);
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
    public void displayCaregiveeList(){
        HashMap<String, ArrayList<String>> listChild = new HashMap<>();
        caregiveeNames.forEach(caregivee -> {
            ArrayList<String> listChildValues = new ArrayList<String>(
                    Arrays.asList("View Profile", "Set Tasks", "See Progress", "Delete Caregivee"));
            listChild.put(caregivee, listChildValues);
        });
        adapter = new MainAdapter(caregiveeNames, listChild);
        caregiveeList.setAdapter(adapter);
        setOnChildListener();
    }

    /**
     * Helper function to view the caregivee profile
     * @param groupPosition the position of the caregivee in the group
     */
    public void viewCaregiveeProfile(int groupPosition){
        String caregiveeName = caregiveeNames.get(groupPosition);
        String caregiveeId = caregiveeIds.get(groupPosition);
        DatabaseReference ref = database.child("users/" + caregiveeId);
        ProfileInfo fragment = new ProfileInfo();
        Bundle args = new Bundle();


        ref.addValueEventListener(new ValueEventListener() {@Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String caregiveeEmail = snapshot.child("email").getValue().toString();
            if (snapshot.child("notes").getValue() != null){
                String caregiveeNotes = snapshot.child("notes").getValue().toString();
                args.putString("caregiveeNotes", caregiveeNotes);
            }
            args.putString("caregiveeEmail", caregiveeEmail);
            args.putString("caregiveeName", caregiveeName);
            fragment.setArguments(args);
            ((Dashboard)getActivity()).replaceActiveFragment(fragment);
        }@Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("error", "Can't query caregivees for this caregiver");
        }
        });
    }

    public void setOnChildListener(){
        caregiveeList.setOnChildClickListener((parent, view, groupPosition, childPosition, id) -> {
            switch(childPosition) {
                case 0:
                    viewCaregiveeProfile(groupPosition);
                    break;
                case 1:
                    ((Dashboard)getActivity()).replaceActiveFragment(new SetTasksFragment(caregiveeIds.get(groupPosition)));
                    break;
                case 2:
                    startActivity(new Intent(getContext(), ViewProgress.class));
                    break;
                case 3:
                    startActivity(new Intent(getContext(), RemoveCaregivee.class));
                    break;
            }
            return true;
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_home_caregiver, container, false);

        // Get the caregivee list and display their information
        caregiveeList = (ExpandableListView) view.findViewById(R.id.caregiveeHomelist);

        // Call firebase to query the caregivees
        queryCaregivees();

        return view;
    }
}