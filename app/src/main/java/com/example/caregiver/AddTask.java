package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.json.JSONArray;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;

public class AddTask extends AppCompatActivity {


    List<String> CAREGIVEE_OPTIONS = new ArrayList<>();
    List<String> ROOM_OPTIONS = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

        // Get data from the tasks page
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String caregiveeStr = preferences.getString("caregiveeArray", null);
        if (caregiveeStr != null){
            Gson gson = new Gson();
            JsonArray caregiveeArray = gson.fromJson(caregiveeStr, JsonArray.class);
            for (JsonElement element : caregiveeArray) {
                JsonObject caregivee = element.getAsJsonObject();
                CAREGIVEE_OPTIONS.add(caregivee.get("name").toString());
            }
        }

        // Set spinner options for beacons
        Spinner beaconSpinner = (Spinner) findViewById(R.id.taskBeacon);
        ArrayAdapter<String> adapter = new ArrayAdapter<String> (
                this, android.R.layout.simple_spinner_item,  ROOM_OPTIONS);
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