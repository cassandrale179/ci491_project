package com.example.caregiver;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class EditTask extends AppCompatActivity {

    private EditText taskNameField;
    private EditText taskNotesField;
    private Spinner caregiveeSpinner;
    private Spinner roomSpinner;

    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_task);

        Intent intent = getIntent();
        TaskFragment.Task currTask = (TaskFragment.Task) intent.getParcelableExtra("currtask");
        String[] caregiveeRooms = intent.getStringArrayExtra("rooms");

        if(currTask == null) return;

        String task = getIntent().getStringExtra("task");
        String room = getIntent().getStringExtra("room");
        String caregiveeId = getIntent().getStringExtra("caregiveeid");

        taskNameField = (EditText) findViewById(R.id.taskName);
        taskNotesField = (EditText) findViewById(R.id.taskNotes);

        taskNameField.setText(currTask.taskName, TextView.BufferType.EDITABLE);
        taskNotesField.setText(currTask.taskNote, TextView.BufferType.EDITABLE);

        Log.i("INFO", "rooms = " + caregiveeRooms.toString());

//        String userId = PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getString("userId", "");
//        DatabaseReference ref = database.child("/users/" + caregiveeId + "/rooms/" + room + "/tasks/");
//        ref.orderByChild("name").equalTo(task).addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                String taskId = snapshot.child("/tasks/").getKey();
//                Log.i("INFO", taskId + snapshot.toString());
////                String notes = snapshot.child("/tasks/" + (String) taskId + "/notes/").getValue().toString();
////
////                taskNotesField.setText(notes, TextView.BufferType.EDITABLE);
//
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });
//        taskNameField.setText(task, TextView.BufferType.EDITABLE);
    }
}
