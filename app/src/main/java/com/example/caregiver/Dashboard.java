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

    String role;
    private BottomNavigationView bottomNavigationView;

    @Override
    public void onBackPressed(){
        // minimize the app if back button is pressed while in home page
        if (bottomNavigationView.getSelectedItemId() == R.id.home){
            moveTaskToBack(true);
        }
        bottomNavigationView.setSelectedItemId(R.id.home);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        bottomNavigationView = findViewById(R.id.bottomNavigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        role = preferences.getString("userRole", "");

        // By default, the app opens the home page
        bottomNavigationView.setSelectedItemId(R.id.home);

        // Hide beacon fragment for Caregiver
        if (role.equals("caregiver"))
        {
            bottomNavigationView.getMenu().findItem(R.id.beacon).setVisible(false);
        }

        // When user clicks on task notification intent, they are redirected to the TaskCaregivee fragment
        String notificationIntentFragment = getIntent().getStringExtra("fragment");
        if (notificationIntentFragment != null) {
            if (notificationIntentFragment.equals("TaskCaregivee")) {
                bottomNavigationView.setSelectedItemId(R.id.task);
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