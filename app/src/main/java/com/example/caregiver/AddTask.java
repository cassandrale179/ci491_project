package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.gson.Gson;

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
    private Spinner caregiveeSpinner;
    private Spinner roomSpinner;
    public String selectedCaregiveeId;
    private EditText taskNameField;
    private EditText taskNotesField;
    private String caregiverId;
    private TextView errorMessage;
    int red;
    int green;

    /**
     * Render the error and success message field.
     * @param sourceString The text message to be displayed.
     * @param color The color for the text message (red for error, green for success).
     */
    public void displayMessage(String sourceString, int color) {
        errorMessage.setText(Html.fromHtml(sourceString));
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setTextColor(color);
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Initialize spinners
        caregiveeSpinner = (Spinner) findViewById(R.id.taskCaregivee);
        roomSpinner = (Spinner) findViewById(R.id.taskRoom);

        // Initialize fields
        taskNameField = (EditText) findViewById(R.id.taskName);
        taskNotesField = (EditText) findViewById(R.id.taskNotes);
        errorMessage = (TextView) findViewById(R.id.taskMessage);

        // Set color
        red = ContextCompat.getColor(getApplicationContext(), R.color.red);
        green = ContextCompat.getColor(getApplicationContext(), R.color.green);

        // Handling create spinner options
        createSpinners();

        // navigate back to dashboard
        Button backButton = findViewById(R.id.taskCancelButton);
        backButton.setOnClickListener(view -> startActivity(new Intent(view.getContext(), Dashboard.class)));
    }

    protected void createSpinners(){
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
    }

    /**
     * Uploads the newly created task to Firebase
     * @param view The view of the Add Task page
     */
    public void CreateTask(View view){
        if (taskNameField.getText().toString().isEmpty() ||
                roomSpinner.getSelectedItem() == null ||
                selectedCaregiveeId == null){
            displayMessage("Some fields are missing.", red);
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
                    displayMessage(databaseError.getMessage(), red);
                } else {
                   displayMessage("Data saved successfully.", green);
                   // navigate back to Dashboard
                   startActivity(new Intent(view.getContext(), Dashboard.class));
                }
            }
        });
    }
}
