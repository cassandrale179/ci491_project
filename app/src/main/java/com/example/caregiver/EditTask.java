package com.example.caregiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;

public class EditTask extends AppCompatActivity {

    // fields in layout
    private EditText taskNameField;
    private EditText taskNotesField;
    private EditText caregiveeField;
    private Spinner roomSpinner;

    private TaskFragment.Task currTask; // current task being edited
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // retrieve info from TaskFragment page intent
        Intent intent = getIntent();
        currTask = intent.getParcelableExtra("currtask");
        String[] caregiveeRooms = intent.getStringArrayExtra("rooms");
        String caregiveeName = intent.getStringExtra("caregiveeName");

        /* TODO handle error */
        // if task is not found, do not display
        if(currTask == null) return;

        // get all field/spinners
        taskNameField = findViewById(R.id.taskName);
        taskNotesField = findViewById(R.id.taskNotes);
        roomSpinner = findViewById(R.id.taskRoom);
        caregiveeField = findViewById(R.id.taskCaregivee);

        // populate all fields with task info & create spinner
        taskNameField.setText(currTask.taskName, TextView.BufferType.EDITABLE);
        taskNotesField.setText(currTask.taskNote, TextView.BufferType.EDITABLE);
        caregiveeField.setText(caregiveeName, TextView.BufferType.NORMAL);
        createSpinner(caregiveeRooms, currTask.room);

        Log.i("INFO", "EditTask:onCreate displayed all task details successfully.");

        /* TODO handle task removal */
    }

    /**
     * Initializes room spinner, creates adapter with list of all caregivee rooms
     * @param allCaregiveeRooms, list of caregivee rooms
     * @param currRoom, room of current task
     */
    protected void createSpinner(String[] allCaregiveeRooms, String currRoom){
        // set curr room to first entry
        for(int i = 0; i < allCaregiveeRooms.length; i++){
            // swap first with curr room
            if(allCaregiveeRooms[i].equals(currRoom)){
                String temp = allCaregiveeRooms[i];
                allCaregiveeRooms[i] = allCaregiveeRooms[0];
                allCaregiveeRooms[0] = temp;
                break;
            }
        }
        // Render list on the caregivee spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (
                this, android.R.layout.simple_spinner_item,  allCaregiveeRooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       roomSpinner.setAdapter(adapter);
    }

    /**
     * Cancel Button onClick function - navigates back to dashboard
     * @param view
     */
    public void NavigateToDashboard(View view){
        // navigate to dashboard after update
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }

    /**
     * Updates task in firebase, triggered when user clicks 'Update'
     * If room is updated, the previous task is removed
     * The updated task is written to the DB
     * @param view
     */
    public void UpdateTask(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String path;
        String updatedRoom = roomSpinner.getSelectedItem().toString();

        // if room changed, remove previous task
        if(!updatedRoom.equals(currTask.room)){
            // if room is changed, remove the old task from its room
            path = createPath(currTask.caregiveeId, currTask.room, currTask.taskId);
            removeTaskInFirebase(path);
        }

        // post updated task details
        String updatedTaskName = String.valueOf(taskNameField.getText());
        String updatedNotes = String.valueOf(taskNotesField.getText());
        // formulate path
        path = createPath(currTask.caregiveeId, updatedRoom, currTask.taskId);
        // create updated task HashMap
        Map<String, Object> updatedTask = new HashMap<>();
        updatedTask.put("assignedStatus", currTask.assignedStatus); // prev value
        updatedTask.put("caregiverID", preferences.getString("userId", null)); // curr user's info
        updatedTask.put("completionStatus", currTask.completionStatus); // prev value
        updatedTask.put("name", updatedTaskName); // updated value
        updatedTask.put("notes", updatedNotes); // updated value

        // post updated task to path in Firebase
        updateTaskInFirebase(path, updatedTask);
    }

    /**
     * Creates path String for Firebase update/removal
     * @param caregiveeId, ID of caregivee
     * @param room, task room
     * @param taskId, task ID
     * @return string with path location
     */
    private String createPath(String caregiveeId, String room, String taskId){
        return "/users/" + caregiveeId + "/rooms/" + room + "/tasks/" + taskId + "/";
    }

    /**
     * In the case of a room change, this removes the task from the previous room
     * @param path, path to task that needs to be removed
     */
    private void removeTaskInFirebase(String path){
        DatabaseReference ref = database.child(path);
        ref.removeValue();
    }

    /**
     * Creates a ref to the inputted path to task id in the DB & updates task details
     * @param path, path to taskId
     * @param updatedTask, HashMap with task details (assignedStatus, CaregiverID, CompletionStatus,
     *                     Name, Notes
     */
    private void updateTaskInFirebase(String path, Map<String, Object> updatedTask){
        DatabaseReference ref = database.child(path);
        ref.updateChildren(updatedTask);

        // navigate to dashboard after update
        Intent intent = new Intent(this, Dashboard.class);
        startActivity(intent);
    }
}
