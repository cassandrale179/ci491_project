package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class AddTask extends AppCompatActivity {

    // This stores the dropdown menu for caregivee names
    List<String> caregivee_spinner_options = new ArrayList<>();

    // This stores the corresponding caregivee id
    List<String> caregivee_spinner_ids = new ArrayList<>();

    // Key is the caregivee id, value is a list of rooms in that caregivee's house
    HashMap< String, List<String> > caregiveeRooms = new HashMap<>();

    // Set up global variables
    Spinner caregiveeSpinner;
    Spinner roomSpinner;
    String selectedCaregiveeId;
    EditText taskNameField;
    EditText taskNotesField;
    String caregiverId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize spinners
        caregiveeSpinner = (Spinner) findViewById(R.id.taskCaregivee);
        roomSpinner = (Spinner) findViewById(R.id.taskRoom);

        // Get data from the tasks page
        Gson gson = new Gson();
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String caregiveeRoomsStr = preferences.getString("caregiveeRooms", null);
        String caregiveeNames = preferences.getString("caregiveeInfo", null);
        caregiverId =  preferences.getString("userId", "");

        if (caregiveeNames != null){
            HashMap< String, String > caregiveeInfo = gson.fromJson(caregiveeNames, HashMap.class);
            caregiveeInfo.forEach((id, name) -> {
                caregivee_spinner_options.add(name);
                caregivee_spinner_ids.add(id);
            });
        }
        if (caregiveeRoomsStr != null){
            caregiveeRooms = gson.fromJson(caregiveeRoomsStr, HashMap.class);
        }

        // Render list on the caregivee spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (
                this, android.R.layout.simple_spinner_item,  caregivee_spinner_options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        caregiveeSpinner.setAdapter(adapter);

        // When user click on selected spinner, we want to get the caregivee id and their rooms.
        caregiveeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View view, int pos, long id) {
                selectedCaregiveeId = caregivee_spinner_ids.get(pos);

                // If caregivee has not defined their room, we give them default value.
                if (caregiveeRooms.size() > 0 && caregiveeRooms.containsKey(selectedCaregiveeId)){
                    List<String> rooms = caregiveeRooms.get(selectedCaregiveeId);
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (
                            AddTask.this, android.R.layout.simple_spinner_item, rooms);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    roomSpinner.setAdapter(adapter2);
                } else {
                    List<String> rooms = Arrays.asList("livingroom", "bedroom", "bathroom", "kitchen");
                    ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (
                            AddTask.this, android.R.layout.simple_spinner_item, rooms);
                    adapter2.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                    roomSpinner.setAdapter(adapter2);
                }
            }
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

        // TODO: for some reason this crash the app after it return to the Tasks page
        taskNameField = (EditText) findViewById(R.id.taskName);
        taskNotesField = (EditText) findViewById(R.id.taskNotes);
        Button backButton = findViewById(R.id.taskBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), TaskFragment.class));
            }
        });
    }

    /**
     * Uploads the newly created task to Firebase
     * @param view The view of the Add Task page
     */
    public void CreateTask(View view){
        if (taskNameField.getText() == null ||
                roomSpinner.getSelectedItem() == null ||
                selectedCaregiveeId == null){
            Log.d("Error", "Your field is missing");
            return;
        }

        String taskName = taskNameField.getText().toString();
        String taskNotes = "N/A";
        String room = roomSpinner.getSelectedItem().toString();
        String uniqueID = UUID.randomUUID().toString();

        DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        DatabaseReference taskRef = database
                .child("users")
                .child(selectedCaregiveeId)
                .child("rooms")
                .child(room)
                .child("tasks");

        if (taskNotesField.getText() != null){
            taskNotes = taskNotesField.getText().toString();
        }

        Map<String, Object> userUpdates = new HashMap<>();
        userUpdates.put(uniqueID+"/name", taskName);
        userUpdates.put(uniqueID+"/notes", taskNotes);
        userUpdates.put(uniqueID+"/caregiverID", caregiverId);
        userUpdates.put(uniqueID+"/assignedStatus", "true");
        userUpdates.put(uniqueID+"/completionStatus", "incomplete");

        taskRef.updateChildren(userUpdates, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {
                if (databaseError != null) {
                    System.out.println("Data could not be saved " + databaseError.getMessage());
                } else {
                    System.out.println("Data saved successfully.");
                }
            }
        });
    }
}
