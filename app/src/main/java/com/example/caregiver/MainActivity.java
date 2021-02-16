package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import android.view.View;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /** Navigation function to move to Identification Screen **/
    public void openIdentification(View v){
        String tag = (String) v.getTag();
        Intent i = new Intent(MainActivity.this, Identification.class);
        i.putExtra("tag", tag);
        startActivity(i);



    }

}