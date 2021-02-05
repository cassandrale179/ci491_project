package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Formatter;

public class Request extends AppCompatActivity {

    final DatabaseReference database = FirebaseDatabase.getInstance().getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
    }

    /**
     * This is the onClick function for the Skip button, which transition user to Home Page
     * @param v The request activity view
     */
    public void transitionToDashboard(View v){
        Intent i = new Intent(Request.this, Dashboard.class);
        startActivity(i);
    }

    /**
     * Display message will display either error message or success message.
     * @param sourceString the message to be display
     * @param colorString color of the message. Error message = #922B21, Success = #148F77
     */
    public void displayMessage(String sourceString, String colorString){
        TextView textView = (TextView) findViewById(R.id.requestMessage);
        textView.setText(Html.fromHtml(sourceString));
        textView.setVisibility(View.VISIBLE);
        textView.setTextColor(Color.parseColor(colorString));
    }
    private static class User {
        private String id;
        private String name;
        private String userEmail;
        private String role;

        public User(String id, String name, String userEmail, String role){
            this.id = id;
            this.name = name;
            this.userEmail = userEmail;
            this.role = role;
        }
    }

    /**
     * Open a dialog box to confirm if recipient want to send an email request to their caregiver
     * or caregivee.
     * @param v the view from request page
     */
    public void openDialogBox(View v){
        EditText emailField = findViewById(R.id.requestEmail);
        String email = emailField.getText().toString();

        if (email.isEmpty()){
            displayMessage("Please enter a valid email address", "#922B21");
        } else {

            String alertMessage = "Want to send email to " + email + "?";
            AlertDialog.Builder builder = new AlertDialog.Builder(Request.this);
            builder.setMessage(alertMessage)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /* TODO Handling email input here and send request logic ... */
                    // verify email exists
                    final DatabaseReference ref = database.child("/users");
                    ref.orderByChild("email").equalTo(email).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String caregiveeId = snapshot.getKey();
                            Log.d("SUCCESS", "onChildAdded: found email with caregivee: " + caregiveeId);
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) {

                        }

                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            String alertMessage = "Cannot find user with email " + email;
                            AlertDialog.Builder builder = new AlertDialog.Builder(Request.this);
                            builder.setMessage(alertMessage);

                            Log.d("FAIL", "Cannot find such user");
                        }
                    });
                    // send request to caregivee
                }
            }).setNegativeButton("Cancel", null);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}