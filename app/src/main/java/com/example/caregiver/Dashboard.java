package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Set;

public class Dashboard extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    String role;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        role = preferences.getString("userRole", "");

        // Set bottom navigation bar
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new my_caregivee()).commit();
    }


    public void replaceActiveFragment(Fragment newFragment)
    {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, newFragment).commit();
    }


    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationMethod = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment fragment = null;
            switch(menuItem.getItemId()){
            case R.id.home:
                fragment = new my_caregivee();
                break;
            case R.id.task:
                if (role.equals("caregivee")){
                    fragment = new TaskCaregivee();
                } else {
                    fragment = new TaskFragment();
                }
                break;
            case R.id.beacon:
                fragment = new BeaconFragment();
                break;
            case R.id.profile:
                fragment = new ProfileFragment();
                break;
            }
            getSupportFragmentManager().beginTransaction().replace(R.id.container, fragment).commit();
            return true;
        }
     };
}