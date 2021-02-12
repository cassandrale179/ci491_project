package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Dashboard extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();
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
                fragment = new TaskFragment();
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