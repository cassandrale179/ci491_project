package com.example.caregiver;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.HashMap;
import java.util.Map;


public class Signup extends AppCompatActivity {


    public String tag; /* user is caregiver or caregivee */
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    final FirebaseDatabase database = FirebaseDatabase.getInstance();
    private DatabaseReference ref = database.getReference("");

    /** Initialize a user class to store their info **/
    public class User {
        public String name;
        public String email;
        public String role;
        public User(String name, String email, String role) {
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    /**
     * Default oncreate function
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        tag = getTag();
    }

    /** Function call to get user identification **/
    protected String getTag(){
        Log.d("this should be call!", "getTag");
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String tag = extras.getString("tag");
            return tag;
        } else {
            Log.d("no tag found", "None");
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
    public void createUser(View v){
        EditText nameField = findViewById(R.id.userName);
        String name = nameField.getText().toString();

        EditText emailField = findViewById(R.id.userEmail);
        String email = emailField.getText().toString();

        EditText passwordField = findViewById(R.id.userPassword);
        String password = passwordField.getText().toString();

        EditText confirmField = findViewById(R.id.userPassword2);
        String confirm = confirmField.getText().toString();

        if (!confirm.equals(password)){
            displayErrorMessage("Password do not match");

        } else if (email.isEmpty() || password.isEmpty()) {
            displayErrorMessage("Email or password fields are empty.");

        }  else if (tag == null){
            displayErrorMessage("No role is assigned. Please quit app and try again.");

        } else {
            displayErrorMessage("");
            callFirebase(email, password, name);
        }
    }

    /**
     * This function make a call to Firebase Auth to sign up the user by email and password
     * then it calls Firebase Database to store the user information.
     * @param email the user email to sign up
     * @param password the password to sign up
     * @param name the name of the user
     */

    public void callFirebase(String email, String password, String name){
        mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, store user info on Database
                FirebaseUser user = mAuth.getCurrentUser();
                DatabaseReference usersRef = ref.child("users");
                Map<String, Object> userObject = new HashMap<>();

                userObject.put(user.getUid(), new User(name, email, tag));
                usersRef.updateChildren(userObject);

                Log.w("success", "createUserWithEmail:success");

            } else {
                // Sign in fails, display a message to the user.
                Log.w("failure", "createUserWithEmail:failure", task.getException());
                displayErrorMessage(task.getException().getMessage());
            }
        });
    }
}