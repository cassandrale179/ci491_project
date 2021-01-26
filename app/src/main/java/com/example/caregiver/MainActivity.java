package com.example.caregiver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.FirebaseDatabase;
import com.kontakt.sdk.android.ble.configuration.ActivityCheckConfiguration;
import com.kontakt.sdk.android.ble.configuration.ForceScanConfiguration;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.EddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleEddystoneListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleScanStatusListener;
import com.kontakt.sdk.android.ble.rssi.RssiCalculators;
import com.kontakt.sdk.android.ble.spec.EddystoneFrameType;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneDevice;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity {

    private BottomNavigationView bottomNavigationView;
    private ProximityManager proximityManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigation);

        bottomNavigationView.setOnNavigationItemSelectedListener(bottomNavigationMethod);
        getSupportFragmentManager().beginTransaction().replace(R.id.container, new HomeFragment()).commit();

        KontaktSDK.initialize("clYwuEPnEpprKHUBKIwTudpdiEqMgMQq");

        proximityManager = ProximityManagerFactory.create(this);
        configureProximityManager();
        proximityManager.setIBeaconListener(createIBeaconListener());
        proximityManager.setEddystoneListener(createEddystoneListener());
        proximityManager.setScanStatusListener((createSimpleScanStatusListener()));
    }

    private void configureProximityManager() {
        proximityManager.configuration()
                .scanMode(ScanMode.BALANCED)
                .scanPeriod(ScanPeriod.RANGING)
                .activityCheckConfiguration(ActivityCheckConfiguration.DISABLED)
                .forceScanConfiguration(ForceScanConfiguration.DISABLED)
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5))
                .rssiCalculator(RssiCalculators.DEFAULT)
                .cacheFileName("Example")
                .resolveShuffledInterval(3)
                .monitoringEnabled(true)
                .monitoringSyncInterval(10)
                .eddystoneFrameTypes(Arrays.asList(EddystoneFrameType.UID, EddystoneFrameType.URL));
    }

    @Override
    protected void onStart() {
        super.onStart();
        startScanning();
    }

    @Override
    protected void onStop() {
        proximityManager.stopScanning();
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        proximityManager.disconnect();
        proximityManager = null;
        super.onDestroy();
    }

    private void startScanning() {
        proximityManager.connect(new OnServiceReadyListener() {
            @Override
            public void onServiceReady() {
                proximityManager.startScanning();
            }
        });
    }

    private SimpleScanStatusListener createSimpleScanStatusListener() {
        return new SimpleScanStatusListener() {
            @Override
            public void onScanStart() {
                Log.i("Sample", "Scanning started");
            }

            @Override
            public void onScanStop() {
                Log.i("Sample", "Scanning stopped");
            }
        };
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {
                Log.i("Sample", "IBeacon discovered: " + ibeacon.getName() + " " + ibeacon.toString());
            }
        };
    }

    private EddystoneListener createEddystoneListener() {
        return new SimpleEddystoneListener() {
            @Override
            public void onEddystoneDiscovered(IEddystoneDevice eddystone, IEddystoneNamespace namespace) {
                Log.i("Sample", "Eddystone discovered: " + eddystone.getName() + " " + eddystone.toString());
            }
        };
    }


    private  BottomNavigationView.OnNavigationItemSelectedListener bottomNavigationMethod = new
            BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    Fragment fragment = null;

                    switch(menuItem.getItemId()){

                        case R.id.home:
                            fragment = new HomeFragment();
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