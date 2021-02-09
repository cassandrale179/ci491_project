package com.example.caregiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class Login extends AppCompatActivity {

    private FirebaseAuth mAuth;

    /**
     * Default onCreate function
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    /**
     * Outputs specified error message to loginMessage textview
     * @param sourceString - specified error message
     */
    private void displayErrorMessage(String sourceString){
        TextView textView = (TextView) findViewById(R.id.loginMessage);
        textView.setText(Html.fromHtml(sourceString));
        textView.setVisibility(View.VISIBLE);
    }

    /**
     * Conducts user sign in thru firebase, navigates to dashboard if successful,
     * else displays err message
     * @param email, user email
     * @param password, user encrypted password
     */
    private void loginFirebaseCall(String email, String password){
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        Log.w("success","signInWithEmail:success");
                        // move to dashboard
                        if ( mAuth.getCurrentUser() != null ) {
                            navigateToDashboard(mAuth.getCurrentUser().getUid());
                        } else {
                            Log.w("failure", "Can't find user id");
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.w("failure", "signInWithEmail:failure", task.getException());
                        displayErrorMessage("Authentication failed. Please try again with a matching email & password.");
                    }
                });
    }

    /**
     * Navigates to Dashboard after successful sign in through Firebase
     */
    private void navigateToDashboard(String userId){
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("userId", userId);
        editor.apply();

        Intent i = new Intent(Login.this, Dashboard.class);
        startActivity(i);
    }

    /**
     * Initiates Login once button is clicked - retrieves email/password, conducts checks
     * and makes call to login thru firebase
     * @param view
     */
    public void initiateLogin(View view) {
        EditText emailField = findViewById(R.id.userEmail);
        String email = emailField.getText().toString();

        EditText passwordField = findViewById(R.id.userPassword);
        String password = passwordField.getText().toString();

        if(email.isEmpty() || password.isEmpty()){
            displayErrorMessage("Email or password fields are empty.");
        } else {
            displayErrorMessage("");
            loginFirebaseCall(email, password);
        }
    }

    /**
     * Navigates to Sign Up page for members without account
     * @param view
     */
    public void navigateSignUp(View view){
        TextView navMessage = findViewById(R.id.signUpNavMessage);
        navMessage.setOnClickListener(v -> {
            Intent intent = new Intent(view.getContext(), Signup.class);
            startActivity(intent);
        });
    }

}