package com.example.caregiver;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class Signup extends AppCompatActivity {

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        mAuth = FirebaseAuth.getInstance();
    }

    public void createUser(View v){
        EditText nameField = findViewById(R.id.userName);
        String name = nameField.getText().toString();

        EditText emailField = findViewById(R.id.userEmail);
        String email = emailField.getText().toString();

        EditText passwordField = findViewById(R.id.userPassword);
        String password = passwordField.getText().toString();

        EditText confirmField = findViewById(R.id.userPassword2);
        String confirm = confirmField.getText().toString();

        if (!email.isEmpty() && !password.isEmpty()){
            Log.d("create", "creating user here!");
            callFirebase(email, password);
        } else {
            Log.d("error", "handle error");
        }
    }

    public void callFirebase(String email, String password){
        mAuth.createUserWithEmailAndPassword(email, password)
        .addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                // Sign in success, update UI with the signed-in user's information
                FirebaseUser user = mAuth.getCurrentUser();
            } else {
                // If sign in fails, display a message to the user.
                Log.w("failure", "createUserWithEmail:failure", task.getException());
            }
        });
    }
}