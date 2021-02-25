package com.example.caregiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

public class EditTask extends AppCompatActivity {

    // fields in layout
    private EditText taskNameField;
    private EditText taskNotesField;
    private EditText caregiveeField;
    private Spinner roomSpinner;
    private TextView errorMessage;

    private TaskFragment.Task currTask; // current task being edited
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        // retrieve info from TaskFragment page intent
        Intent intent = getIntent();
        currTask = intent.getParcelableExtra("currTask");
        String[] caregiveeRooms = intent.getStringArrayExtra("rooms");
        String caregiveeName = intent.getStringExtra("caregiveeName");

        /* TODO better error handling */
        // if task is not found, do not display
        if(currTask == null){
            Log.e("FAIL", "EditTask:onCreate could not get selectedTask from TaskFragment.");
            return;
        }

        // get all field/spinners
        taskNameField = findViewById(R.id.taskName);
        taskNotesField = findViewById(R.id.taskNotes);
        roomSpinner = findViewById(R.id.taskRoom);
        caregiveeField = findViewById(R.id.taskCaregivee);
        errorMessage = findViewById(R.id.taskMessage);

        // populate all fields with task info & create spinner
        taskNameField.setText(currTask.taskName, TextView.BufferType.EDITABLE);
        taskNotesField.setText(currTask.taskNote, TextView.BufferType.EDITABLE);
        caregiveeField.setText(caregiveeName, TextView.BufferType.NORMAL);
        createSpinner(caregiveeRooms, currTask.room);

    }

    /**
     * Initializes room spinner, creates adapter with list of all caregivee rooms
     * @param allCaregiveeRooms, list of caregivee rooms
     * @param currRoom, room of current task
     */
    protected void createSpinner(String[] allCaregiveeRooms, String currRoom){
        // swap first with curr room
        int currentRoomIndex = Arrays.asList(allCaregiveeRooms).indexOf(currRoom);
        String temp = allCaregiveeRooms[currentRoomIndex];
        allCaregiveeRooms[currentRoomIndex] = allCaregiveeRooms[0];
        allCaregiveeRooms[0] = temp;

        // Render list on the caregivee spinner
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (
                this, android.R.layout.simple_spinner_item,  allCaregiveeRooms);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
       roomSpinner.setAdapter(adapter);
    }

    /**
     * Back Button onClick function - navigates back to dashboard
     * @param view
     */
    public void navigateToDashboard(View view){
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
    public void updateTask(View view) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String path;
        String updatedRoom = roomSpinner.getSelectedItem().toString();

        // if room changed, remove the old task from its room
        if(!updatedRoom.equals(currTask.room)){
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
     * Removes Task from Firebase after alerting user to confirm action
     * @param view,
     */
    public void deleteTask(View view){
        Log.d("error", "we made it here!");
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(this);
                alertDialog.setMessage("Would you like to delete this task?");
                alertDialog.setNegativeButton("Cancel", null);
                alertDialog.setPositiveButton("Delete Task", (dialog, which) -> {
                    String path = createPath(currTask.caregiveeId, currTask.room, currTask.taskId);
                    removeTaskInFirebase(path);
                    int green = ContextCompat.getColor(getApplicationContext(), R.color.green);
                    displayMessage("Task deleted.", green);
                    navigateToDashboard(view);
                });
        alertDialog.create();
        alertDialog.show();
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
     * Render the error and success message field.
     * @param sourceString The text message to be displayed.
     * @param color The color for the text message (red for error, green for success).
     */
    public void displayMessage(String sourceString, int color) {


        errorMessage.setText(Html.fromHtml(sourceString));
        errorMessage.setVisibility(View.VISIBLE);
        errorMessage.setTextColor(color);
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
        // Set color for success/error messages
        int red = ContextCompat.getColor(getApplicationContext(), R.color.red);
        int green = ContextCompat.getColor(getApplicationContext(), R.color.green);

        DatabaseReference ref = database.child(path);
        ref.updateChildren(updatedTask, (databaseError, databaseReference) -> {
            if (databaseError == null) {
                displayMessage("Your task is updated", green);
                // navigate to dashboard after update
                Intent intent = new Intent(this, Dashboard.class);
                startActivity(intent);
            } else {
                displayMessage("Your task cannot be updated", red);
            }
        });
    }
}
