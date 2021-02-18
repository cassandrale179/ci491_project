package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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

    public void sendFirebaseRequest(DatabaseReference ref, String caregiveeId){
        // add request to caregivee
        Map<String, Object> map = new HashMap<>();
        // Get current userId
        SharedPreferences preferences = PreferenceManager
                .getDefaultSharedPreferences(getApplicationContext());
        String userId = preferences.getString("userId", "");
        String name = preferences.getString("name", "");
        // update map & save to firebase
        map.put(caregiveeId + "/requests/" + userId, name);
        ref.updateChildren(map);
        Log.d("SUCCESS", "sendFirebaseRequest:request sent to caregivee id " + caregiveeId);
    }

    // alert messages & navigate to dashboard
    private void displayRequestSent(){
        // inform user of sent request
        String alertReqSent = "Request sent! Please wait for this request to be approved.";
        AlertDialog.Builder builderReqSent = new AlertDialog.Builder(Request.this)
                .setMessage(alertReqSent)
                .setPositiveButton("Okay",(dialog, which) -> {
                    // navigate to dashboard
                    Intent i = new Intent(Request.this, Dashboard.class);
                    startActivity(i);
                });
        builderReqSent.create().show();
    }
    // alert messages
    private void displayNonExistingCaregivee(){
        // display message of non existing user
        String alertNonCaregivee = "It looks this user is not a registered Caregivee. " +
                "Please enter a valid Caregivee's email.";
        AlertDialog.Builder builderReqSent = new AlertDialog.Builder(Request.this)
                .setMessage(alertNonCaregivee)
                .setPositiveButton("Okay", null);
        builderReqSent.create().show();
    }

    /**
     * method is used for checking valid email id format.
     *
     * @param email
     * @return boolean true for valid false for invalid
     */
    public static boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[A-Z]{2,4}$";
        Pattern pattern = Pattern.compile(expression, Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    /**
     * Open a dialog box to confirm if recipient want to send an email request to their caregiver
     * or caregivee.
     * @param v the view from request page
     */
    public void openDialogBox(View v){
        EditText emailField = findViewById(R.id.requestEmail);
        String email = emailField.getText().toString();

        // check for valid email
        if(!isEmailValid(email)){
            displayMessage("Please enter a valid email address", "#922B21");
        } else {
            String alertMessage = "Would you like to send a request to " + email + " to be a Caregivee?";
            AlertDialog.Builder builder = new AlertDialog.Builder(Request.this);
            builder.setMessage(alertMessage)
                .setPositiveButton("Send", (dialog, which) -> {
                    // verify email exists
                    final DatabaseReference ref = database.child("/users/");
                    ref.orderByChild("email").equalTo(email).addChildEventListener(new ChildEventListener() {
                        @Override
                        public void onChildAdded(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) {
                            String caregiveeId = snapshot.getKey();
                            String role = snapshot.child("/role").getValue().toString();
                            Log.d("INFO", "onChildAdded:caregiveeId = " + caregiveeId + ", role = " + role );
                            // confirm caregivee
                            if(caregiveeId != null && !caregiveeId.isEmpty() && role.equals("caregivee")){
                                // send request to firebase, display message and transition to dashboard
                                sendFirebaseRequest(ref,caregiveeId);
                                displayRequestSent();
                                Log.i("SUCCESS", "onChildAdded:found email with caregivee: " + caregiveeId + " and role: " + role);
                            } else {
                                displayNonExistingCaregivee();
                                Log.d("INFO", "onChildAdded:could not add caregivee");
                            }
                        }

                        @Override
                        public void onChildChanged(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                        @Override
                        public void onChildRemoved(@NonNull DataSnapshot snapshot) { }
                        @Override
                        public void onChildMoved(@NonNull DataSnapshot snapshot, @Nullable String previousChildName) { }
                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            displayNonExistingCaregivee();
                            Log.d("FAIL", "onChildAdded:" + error.toException());
                        }
                    });
                }).setNegativeButton("Cancel", null);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}