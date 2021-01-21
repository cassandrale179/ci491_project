package com.example.caregiver;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class Login extends AppCompatActivity {

    /**
     * Default oncreate function
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void initiateLogin(View view) {
    }

    /**
     * Navigates to Sign Up page for non-members
     * @param view
     */
    public void navigateSignUp(View view){
        TextView navMessage = findViewById(R.id.signUpNavMessage);
        navMessage.setOnClickListener(new View.OnClickListener(){
            public void onClick(View v){
                Intent intent = new Intent(view.getContext(), Signup.class);
                startActivity(intent);
            }
        });
    }

}
