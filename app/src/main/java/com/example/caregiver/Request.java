package com.example.caregiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

public class Request extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
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

    /**
     * Open a dialog box to confirm if recipient want to send an email request to their caregiver
     * or caregivee.
     * @param v the view from request page
     */
    public void openDialogBox(View v){
        EditText emailField = findViewById(R.id.requestEmail);
        String email = emailField.getText().toString();

        if (email.isEmpty()){
            displayMessage("Email input is blank", "#922B21");
        } else {

            String alertMessage = "Want to send email to " + email + "?";
            AlertDialog.Builder builder = new AlertDialog.Builder(Request.this);
            builder.setMessage(alertMessage)
                .setPositiveButton("Send", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    /** Handling email input here and send request logic ... */
                }
            }).setNegativeButton("Cancel", null);
            AlertDialog alert = builder.create();
            alert.show();
        }
    }
}