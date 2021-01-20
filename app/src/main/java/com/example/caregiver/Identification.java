package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

public class Identification extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String identity = extras.getString("tag");
            TextView textView = (TextView) findViewById(R.id.identificationLabel);
            String sourceString = "You identified as a <b>" + identity + "</b> " + ". Login or Sign Up now to access the app!";
            textView.setText(Html.fromHtml(sourceString));

        }
    }
}