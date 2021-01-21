package com.example.caregiver;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

public class Identification extends AppCompatActivity {

    public String tag;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_identification);

        /** Get the user role as caregiver or caregivee and display it */
        tag = setTag();
        TextView textView = (TextView) findViewById(R.id.identificationLabel);
        String sourceString = "You identified as a <b>" + tag + "</b>. Login or Sign Up now to access the app!";
        textView.setText(Html.fromHtml(sourceString));
    }

    /** Function call to get user identification **/
    protected String setTag(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String tag = extras.getString("tag");
            return tag;
        } else {
            return "None";
        }
    }

    /** Navigation function to move to sign up page **/
    public void openSignUp(View v){
        Intent i = new Intent(Identification.this, Signup.class);
        i.putExtra("tag", tag);
        startActivity(i);
    }

    /** Navigation function to move to login page **/
    public void openLogIn(View v){
        Intent i = new Intent(Identification.this, Login.class);
        i.putExtra("tag", tag);
        startActivity(i);
    }
}