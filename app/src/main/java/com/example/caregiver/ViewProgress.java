package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.caregiver.model.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ViewProgress extends AppCompatActivity {
    private String caregiveeName;
    private String caregiveeID;
    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_progress);

        // get our fields from the extras
        caregiveeName = getIntent().getStringExtra("caregiveeName");
        caregiveeID = getIntent().getStringExtra("caregiveeID");

        // Set the text to use the correct caregivee name
        TextView nameText = findViewById(R.id.progressNameText);
        nameText.setText("Progress chart for " + caregiveeName);

        // Setup back button
        ImageButton backArrow = findViewById(R.id.backArrowButton);
        backArrow.setOnClickListener(view -> {
            onBackPressed();
        });

        loadCaregiveesTask();

    }

    /** Loads all tasks associated with this caregivee */
    protected void loadCaregiveesTask() {
        final DatabaseReference ref= database.child("/users/" + caregiveeID);
        ref.addValueEventListener(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)@Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Object roomObject = snapshot.child("rooms").getValue();
                if (roomObject != null) {
                    List<Task> taskList = Task.getCompletedTaskList(caregiveeID, roomObject);
                    for (Task t : taskList){
                        Log.d("t", t.taskName);
                    }
                }
            }@Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.d("error", "Can't query caregivees for this caregiver");
            }
        });
    }
}