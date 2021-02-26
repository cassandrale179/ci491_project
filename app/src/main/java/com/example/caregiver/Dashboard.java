package com.example.caregiver;


import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class Dashboard extends AppCompatActivity {

    String role;
    private BottomNavigationView bottomNavigationView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        role = preferences.getString("userRole", "");

        if (role.equals("caregiver")) {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeCaregiver()).commit();
            bottomNavigationView.getMenu().findItem(R.id.beacon).setVisible(false);
        } else {
            getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeCaregivee()).commit();
        }

        // When user clicks on notification intent, they are redirected to the TaskCaregivee fragment
        String notificationIntentFragment = getIntent().getStringExtra("fragment");
        if (notificationIntentFragment != null) {
            if (notificationIntentFragment.equals("TaskCaregivee")) {
                getSupportFragmentManager().beginTransaction().replace(R.id.container, new TaskCaregivee()).commit();
            }
        }
    }
    

    private BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationMethod = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
            Fragment fragment = null;
            switch(menuItem.getItemId()){
            case R.id.home:
                if (role.equals("caregiver")){
                    fragment = new HomeCaregiver();
                } else {
                    fragment = new HomeCaregivee();
                }
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

    public void replaceActiveFragment(Fragment newFragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.container, newFragment).commit();
    }
}