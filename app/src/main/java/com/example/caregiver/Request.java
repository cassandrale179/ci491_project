package com.example.caregiver;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class Request extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_request);
    }

    public void openDialogBox(View v){
        AlertDialog.Builder builder = new AlertDialog.Builder(Request.this);
        builder.setMessage("Send email?").setPositiveButton("Send", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                System.out.println("Handling email request here");
            }
        }).setNegativeButton("Cancel", null);


        AlertDialog alert = builder.create();
        alert.show();
    }
}