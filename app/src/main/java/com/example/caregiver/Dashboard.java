package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;

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
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeCaregiver()).commit();
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
                fragment = new HomeCaregiver();
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