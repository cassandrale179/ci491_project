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

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import android.content.SharedPreferences;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TaskFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TaskFragment extends Fragment {

    // Create global variables
    ExpandableListView caregiveeList;
    DatabaseReference database = FirebaseDatabase.getInstance().getReference();
    List<Object> caregivees = new ArrayList<>();
    MainAdapter adapter;

    public TaskFragment() { }

    // TODO: Rename and change types and number of parameters
    public static TaskFragment newInstance(String param1, String param2) {
        TaskFragment fragment = new TaskFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Display all loaded caregivee on the screen after all data is loaded from Firebase.
     */
    protected void displayCaregivees() {
        ArrayList < String > caregiveeNames = new ArrayList < >();
        HashMap< String, ArrayList < String> > listChild = new HashMap<>();

//
//        for (JsonElement caregivee : caregiveeArray ){
//            JsonObject obj = caregivee.getAsJsonObject();
//            String name = obj.get("name").toString().replace("\"", "");
//            caregiveeNames.add(name);
//        }
//
//        adapter = new MainAdapter(caregiveeNames, listChild);
//        caregiveeList.setAdapter(adapter);
//
//        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
//        SharedPreferences.Editor editor = preferences.edit();
//        editor.putString("caregiveeArray", caregiveeArray.toString());
//        editor.apply();
    }

    /**
     * This function create a caregivee object that look like its representation on Firebase.
     * @param caregiveeId the id of the caregivee
     * @param size the size of the caregivees list
     */
    protected void getCaregiveeNameAndTask(String caregiveeId, int size) {
        DatabaseReference ref = database.child("users/" + caregiveeId);
        JsonObject caregivee = new JsonObject();
        ref.addValueEventListener(new ValueEventListener() {@Override
        public void onDataChange(DataSnapshot dataSnapshot) {
            String name = dataSnapshot.child("name").getValue().toString();
            Object rooms = dataSnapshot.child("room").getValue();
            if (rooms != null){
                try {
                    JSONObject jsonObj = new JSONObject(rooms.toString());
                    Iterator keysToCopyIterator = jsonObj.keys();
                    List<String> keysList = new ArrayList<String>();
                    while(keysToCopyIterator.hasNext()) {
                        String key = (String) keysToCopyIterator.next();
                        keysList.add(key);
                        Log.d("key", key);
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }

            // TODO: this is very hacky way of trying to fill the main adapter not using async.
            // TODO: we might run into case where caregivee id doesn't exist.
            if (caregivees.size() == size) {
                displayCaregivees();
            }
        }@Override
        public void onCancelled(DatabaseError databaseError) {
            Log.d("failure", "Unable to obtain caregivee data.");
        }});
    }

    /**
     * This function return the list of caregivees associated with the caregiver.
     * @param view The view of the task page
     */
    public void queryCaregivees(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getActivity());
        String userId = preferences.getString("userId", "");
        DatabaseReference ref = database.child("users/" + userId);
        caregiveeList = (ExpandableListView) view.findViewById(R.id.caregiveelist);

        ref.addValueEventListener(new ValueEventListener() {@Override
        public void onDataChange(@NonNull DataSnapshot snapshot) {
            String value = snapshot.child("caregivees").getValue().toString();
            List < String > caregivees = Arrays.asList(value.split("\\s*,\\s*"));
            for (int i = 0; i < caregivees.size(); i++) {
                getCaregiveeNameAndTask(caregivees.get(i), caregivees.size());
            }
        }@Override
        public void onCancelled(@NonNull DatabaseError error) {
            Log.d("error", "Can't query caregivees for this caregiver");
        }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_task, container, false);

        // Query caregivees for this caregiver
        queryCaregivees(view);

        // Redirect to add task page for floating + button
        FloatingActionButton button = (FloatingActionButton) view.findViewById(R.id.addTaskButton);
        button.setOnClickListener(new View.OnClickListener() {@Override
        public void onClick(View view) {
            startActivity(new Intent(view.getContext(), AddTask.class));
        }
        });

        return view;
    }
}