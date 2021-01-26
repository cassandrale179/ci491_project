package com.example.caregiver.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.device.BeaconRegion;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleScanStatusListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.RemoteBluetoothDevice;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BeaconScanService extends Service {

    private ProximityManager proximityManager;
    private boolean isRunning; // Flag indicating if service is already running.
    
    public static Intent createIntent(final Context context) {
        return new Intent(context, ForegroundScanService.class);
      }

    @Override
    public void onCreate() {
        super.onCreate();
        setupProximityManager();
        isRunning = false;
        proximityManager.setScanStatusListener((createSimpleScanStatusListener()));
    }

    private void setupProximityManager() {
        // Create proximity manager instance
        proximityManager = ProximityManagerFactory.create(this);
    
        // Configure proximity manager basic options
        proximityManager.configuration()
            //Scans for 10 minutes with 2 second pauses to avoid getting killed by Android
            .scanPeriod(ScanPeriod.create(TimeUnit.MINUTES.toMillis(10), TimeUnit.SECONDS.toMillis(2)))
            //Using BALANCED for best performance/battery ratio
            .scanMode(ScanMode.BALANCED);
    
        // Set up iBeacon listener
        proximityManager.setIBeaconListener(createIBeaconListener());
      }

@Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (STOP_SERVICE_ACTION.equals(intent.getAction())) {
      stopSelf();
      return START_NOT_STICKY;
    }

    // Check if service is already active
    if (isRunning) {
      Toast.makeText(this, "Service is already running.", Toast.LENGTH_SHORT).show();
      return START_STICKY;
    }

    startInForeground();
    startScanning();
    isRunning = true;
    return START_STICKY;
  }


}
