package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;

public class RemoveCaregivee extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_remove_caregivee);

        Button cancelButton = findViewById(R.id.cancel_button);
        Button confirmButton = findViewById(R.id.confirm_button);

        cancelButton.setOnClickListener(view ->
        {
           // Set up button to do stuff here
        });

        confirmButton.setOnClickListener(view ->
        {
            // Set up button to do stuff here
        });
    }
}