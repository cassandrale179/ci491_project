package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class AddTask extends AppCompatActivity {

    public static final String[] BEACON_OPTIONS  = {"Bathroom", "Kitchen", "Bedroom"};
    public static final String[] CAREGIVEE_OPTIONS  = {"Mary Yu", "John Smith", "Robert Ng."};


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Set spinner options for beacons
        Spinner beaconSpinner = (Spinner) findViewById(R.id.taskBeacon);
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (
                this, android.R.layout.simple_spinner_item,  BEACON_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        beaconSpinner.setAdapter(adapter);

        // Set spinner options for caregivee
        Spinner caregiveeSpinner = (Spinner) findViewById(R.id.taskCaregivee);
        ArrayAdapter<String> adapter2 = new ArrayAdapter<String> (
                this, android.R.layout.simple_spinner_item,  CAREGIVEE_OPTIONS);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        caregiveeSpinner.setAdapter(adapter2);

        // Redirect to add task page for floating + button
        Button backButton = findViewById(R.id.taskBackButton);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(view.getContext(), TaskFragment.class));
            }
        });
    }
}