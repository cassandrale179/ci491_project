package com.example.caregiver;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.example.caregiver.model.User;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static com.example.caregiver.BeaconRegionList.kontaktUUID;
import static com.example.caregiver.BeaconRegionList.scanServiceIntent;


public class Signup extends AppCompatActivity {

    // User is caregiver or caregivee
    public String userRole;
    private FirebaseAuth mAuth;
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private final DatabaseReference ref = database.getReference("");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        setContentView(R.layout.activity_signup);
        userRole = getUserRole();
        // hide kontaktUUID field from caregiver
        if (userRole.equals("caregiver")){
            findViewById(R.id.kontaktUUID).setVisibility(View.GONE);
        }
    }

    /** Function call to check whether user is caregiver or caregivee **/
    protected String getUserRole(){
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            return extras.getString("userRole");
        } else {
            return "None";
        }
    }

    public void displayErrorMessage(String sourceString){
        TextView textView = (TextView) findViewById(R.id.signupMessage);
        textView.setText(Html.fromHtml(sourceString));
        textView.setVisibility(View.VISIBLE);
    }

    /**
     * This function takes in the text fields label on sign up page
     * @param v This is the view in activity_signup.xml
     */
    public void createUser(View v) {
        EditText nameField = findViewById(R.id.userName);
        String name = nameField.getText().toString();

        EditText emailField = findViewById(R.id.userEmail);
        String email = emailField.getText().toString();

        EditText passwordField = findViewById(R.id.userPassword);
        String password = passwordField.getText().toString();

        EditText confirmField = findViewById(R.id.userPassword2);
        String confirm = confirmField.getText().toString();

        String UUIDValue = null;
        if (userRole.equals("caregivee")) {
            EditText kontaktUUIDField = findViewById(R.id.kontaktUUID);
            UUIDValue = kontaktUUIDField.getText().toString();
        }

        if (!confirm.equals(password)) {
            displayErrorMessage("Password do not match");

        } else if (email.isEmpty() || password.isEmpty()) {
            displayErrorMessage("Email or password fields are empty.");

        } else if (userRole == null) {
            displayErrorMessage("No role is assigned. Please quit app and try again.");
        } else if (userRole.equals("caregivee") && UUIDValue.isEmpty()) {
            displayErrorMessage("Kontakt UUID field is empty.");

        } else {
            displayErrorMessage("");
            callFirebase(email, password, name, UUIDValue);
        }
    }

    /**
     * This function make a call to Firebase Auth to sign up the user by email and password
     * then it calls Firebase Database to store the user information.
     * @param email the user email to sign up
     * @param password the password to sign up
     * @param name the name of the user
     */

    public void callFirebase(String email, String password, String name, String UUIDValue){
        mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, store user info on Database
                FirebaseUser user = mAuth.getCurrentUser();
                DatabaseReference usersRef = ref.child("users");
                Map<String, Object> userObject = new HashMap<>();

                userObject.put(user.getUid(), new User(name, email, userRole));
                usersRef.updateChildren(userObject);

                if (UUIDValue != null)
                {
                    usersRef.child(user.getUid()).child("uuid").setValue(UUIDValue);
                }

                // store in current session shared preferences
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = preferences.edit();
                editor.putString("userId", user.getUid());
                editor.putString("userName", name);
                editor.putString("userEmail", email);
                editor.putString("userRole", userRole);
                editor.apply();

                Intent i = new Intent(Signup.this, Request.class);
                startActivity(i);

            } else {
                // Sign in fails, display a message to the user.
                Log.w("failure", "createUserWithEmail:failure", task.getException());
                displayErrorMessage(task.getException().getMessage());
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    public void setUUIDinFirebase(String kontaktUUID) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            DatabaseReference rootRef = FirebaseDatabase.getInstance().getReference();
            rootRef.child("users").child(user.getUid()).child("uuid").setValue(kontaktUUID);
    }
}