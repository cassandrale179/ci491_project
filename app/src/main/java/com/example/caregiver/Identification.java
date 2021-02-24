package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Identification extends AppCompatActivity {

    // User role = "caregivee" if user is caregivee, and "caregiver" otherwise.
    public String userRole;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);


        userRole = getUserRole();
        TextView textView = (TextView) findViewById(R.id.identificationLabel);
        String sourceString = "You identified as a <b>" + userRole + "</b>. Login or Sign Up now to access the app!";
        textView.setText(Html.fromHtml(sourceString));
    }

    /** Get userRole (caregiver or caregivee)  */
    protected String getUserRole(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return extras.getString("userRole");
        } else {
            return "None";
        }
    }

    /** Navigation function to move to sign up page **/
    public void openSignUp(View v){
        Intent i = new Intent(Identification.this, Signup.class);
        i.putExtra("userRole", userRole);
        startActivity(i);
    }

    /** Navigation function to move to login page **/
    public void openLogIn(View v){
        Intent i = new Intent(Identification.this, Login.class);
        startActivity(i);
    }
}