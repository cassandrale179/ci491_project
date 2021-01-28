package com.example.caregiver.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
//import android.support.annotation.Nullable;
//import android.support.annotation.RequiresApi;
//import android.support.v4.app.NotificationCompat;
//import android.support.v4.app.NotificationManagerCompat;
import android.util.Log;
//import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.device.BeaconRegion;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.SpaceListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleScanStatusListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleSpaceListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;
import com.kontakt.sdk.android.common.profile.IEddystoneNamespace;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class BeaconScanService extends Service {

  private static final String STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION";
  private static final CharSequence NOTIFICATION_CHANEL_NAME = "Caregiver channel name";
  private ProximityManager proximityManager;
    private boolean isRunning; // Flag indicating if service is already running.
    private static final String DEFAULT_CHANNEL_ID = "Caregiver_Channel_ID";
    private Intent serviceIntent;
    public static final String TAG = "ProximityManager";
    
    public static Intent createIntent(final Context context) {
        return new Intent(context, BeaconScanService.class);
      }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        KontaktSDK.initialize("clYwuEPnEpprKHUBKIwTudpdiEqMgMQq");
        setupProximityManager();
        setupSpaces();
        createNotificationChannel();

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

        //Setting up iBeacon and Eddystone spaces listeners
        createSpaceListener();
    
        // Set up iBeacon listener
        proximityManager.setIBeaconListener(createIBeaconListener());
      }
      
      //TODO: When user sets up the regions, send UUID and region as input here
      private void setupSpaces() {
        //Setting up single iBeacon region. Put your own desired values here.
        IBeaconRegion region1 = new BeaconRegion.Builder().identifier("Mannika Bedroom")
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) //TODO: Add users UUID
                //Optional major and minor values
                .major(1)
                .minor(2)
                .build();
        proximityManager.spaces().iBeaconRegion(region1);
    
        IBeaconRegion region2 = new BeaconRegion.Builder().identifier("Mannika Bathroom") //Region identifier is mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) ///TODO: Add users UUID
                //Optional major and minor values
                .major(1)
                .minor(1)
                .build();
        proximityManager.spaces().iBeaconRegion(region2);
    
        IBeaconRegion region3 = new BeaconRegion.Builder().identifier("Mannika Kitchen") //Region identifier is mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) //TODO: Add users UUID
                //Optional major and minor values
                .major(1)
                .minor(3)
                .build();
        proximityManager.spaces().iBeaconRegion(region3);

        IBeaconRegion region4 = new BeaconRegion.Builder().identifier("Jui Bedroom") //Region identifier is mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) //TODO: Add users UUID
                //Optional major and minor values
                .major(2)
                .minor(2)
                .build();
        proximityManager.spaces().iBeaconRegion(region4);
    
        IBeaconRegion region5 = new BeaconRegion.Builder().identifier("Jui Bathroom") //Region identifier is mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) ///TODO: Add users UUID
                //Optional major and minor values
                .major(2)
                .minor(1)
                .build();
        proximityManager.spaces().iBeaconRegion(region5);
    
        IBeaconRegion region6 = new BeaconRegion.Builder().identifier("Jui Kitchen") //Region identifier is mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) //TODO: Add users UUID
                //Optional major and minor values
                .major(2)
                .minor(3)
                .build();
        proximityManager.spaces().iBeaconRegion(region6);
      }

  private void createSpaceListener() {
     proximityManager.setSpaceListener(new SimpleSpaceListener() {
      @Override
      public void onRegionEntered(IBeaconRegion region) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(BeaconScanService.this, DEFAULT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.alert_dark_frame)
                .setContentTitle("Region Entered")
                .setContentText(region.getIdentifier());

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BeaconScanService.this);
        notificationManager.notify(0, builder.build());
      }

      @Override
      public void onRegionAbandoned(IBeaconRegion region) {

        NotificationCompat.Builder builder = new NotificationCompat.Builder(BeaconScanService.this, DEFAULT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.alert_dark_frame)
                .setContentTitle("Region Abandoned")
                .setContentText(region.getIdentifier());

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BeaconScanService.this);
        notificationManager.notify(0, builder.build());
      }
    });
  }
      
//      private SpaceListener createSpaceListener() {
//        return new SpaceListener() {
//          @Override
//          public void onRegionEntered(IBeaconRegion region) {
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(BeaconScanService.this, DEFAULT_CHANNEL_ID)
//                    .setSmallIcon(android.R.drawable.alert_dark_frame)
//                    .setContentTitle("Region Entered")
//                    .setContentText(region.getIdentifier());
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BeaconScanService.this);
//            notificationManager.notify(0, builder.build());
//          }
//
//          @Override
//          public void onRegionAbandoned(IBeaconRegion region) {
//
//            NotificationCompat.Builder builder = new NotificationCompat.Builder(BeaconScanService.this, DEFAULT_CHANNEL_ID)
//                    .setSmallIcon(android.R.drawable.alert_dark_frame)
//                    .setContentTitle("Region Abandoned")
//                    .setContentText(region.getIdentifier());
//
//            NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BeaconScanService.this);
//            notificationManager.notify(0, builder.build());
//          }
//
//          @Override
//          public void onNamespaceEntered(IEddystoneNamespace namespace) {
//
//          }
//
//          @Override
//          public void onNamespaceAbandoned(IEddystoneNamespace namespace) {
//
//          }
//        };
//      }

@Override
  public int onStartCommand(Intent intent, int flags, int startId) {
    if (STOP_SERVICE_ACTION.equals(intent.getAction())) {
      stopSelf();
      return START_NOT_STICKY;
    }

  if (isRunning) {
//    Toast.makeText(this, "Service is already running.", Toast.LENGTH_SHORT).show();
    return START_STICKY;
  }

    startInForeground();
    startScanning();
    isRunning = true;
    return START_STICKY;
  }

  @Nullable
  @Override
  public IBinder onBind(Intent intent) {
    return null;
  }

  @Override
  public void onDestroy() {
    if (proximityManager != null) {
      proximityManager.disconnect();
      proximityManager = null;
    }
    super.onDestroy();
  }

  private void startInForeground() {
    // Create notification intent
    final Intent notificationIntent = new Intent();
    final PendingIntent pendingIntent = PendingIntent.getActivity(
        this,
        0,
        notificationIntent,
        0
    );

    // Create stop intent with action
    final Intent intent = BeaconScanService.createIntent(this);
    intent.setAction(STOP_SERVICE_ACTION);
    final PendingIntent stopIntent = PendingIntent.getService(
        this,
        0,
        intent,
        PendingIntent.FLAG_CANCEL_CURRENT
    );

    // Create notification channel
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      createNotificationChannel();
    }

    // Build notification
    final NotificationCompat.Action action = new NotificationCompat.Action(0, "Stop", stopIntent);
    final Notification notification = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
        .setContentTitle("Caregiver")
        .setContentText("Actively scanning beacons")
        .addAction(action)
        .setSmallIcon(android.R.mipmap.sym_def_app_icon) // TODO: Add caregiver icon
        .setContentIntent(pendingIntent)
        .build();

    // Start foreground service
    startForeground(1, notification);
  }

  @RequiresApi(api = Build.VERSION_CODES.O)
  private void createNotificationChannel() {
    final NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    if (notificationManager == null) return;

    final NotificationChannel channel = new NotificationChannel(
        DEFAULT_CHANNEL_ID,
        NOTIFICATION_CHANEL_NAME,
        NotificationManager.IMPORTANCE_DEFAULT
    );
    notificationManager.createNotificationChannel(channel);
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

      @Override
      public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
        super.onIBeaconLost(ibeacon, region);
        Log.e(TAG, "onIBeaconLost: " + ibeacon.getName() + " " + ibeacon.toString());
      }
    };
  }
}