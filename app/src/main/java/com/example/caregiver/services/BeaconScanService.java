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
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.caregiver.BeaconRegionList;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.connection.OnServiceReadyListener;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleIBeaconListener;
import com.kontakt.sdk.android.ble.manager.listeners.simple.SimpleScanStatusListener;
import com.kontakt.sdk.android.common.KontaktSDK;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import static com.example.caregiver.BeaconRegionList.regionRssiMap;

public class BeaconScanService extends Service {

    private static final String STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION";
    private static final CharSequence NOTIFICATION_CHANEL_NAME = "Caregiver channel name";
    private static final String DEFAULT_CHANNEL_ID = "Caregiver_Channel_ID";
    private static final int thirtySecondsInMillis = 30000;
    private static String lastSeenRegionIdentifier = "";
    private ProximityManager proximityManager;
    private boolean isRunning; // Flag indicating if service is already running.
    private long lastTimeInMillis = getTimeNow();
    private double distanceThreshold = 1.5;

    public static Intent createIntent(final Context context) {
        return new Intent(context, BeaconScanService.class);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        // Input parameter is API Key
        KontaktSDK.initialize("clYwuEPnEpprKHUBKIwTudpdiEqMgMQq");
        setupProximityManager();
        createNotificationChannel();
        isRunning = false;
    }


    private void setupProximityManager() {
        // Create proximity manager instance
        proximityManager = ProximityManagerFactory.create(this);

        // Configure proximity manager basic options
        proximityManager.configuration()
                // Scans for 10 minutes with 2 second pauses to avoid getting killed by Android
                .scanPeriod(ScanPeriod.create(TimeUnit.MINUTES.toMillis(10), TimeUnit.SECONDS.toMillis(2)))
                // Using BALANCED for best performance/battery ratio
                .scanMode(ScanMode.BALANCED);

        // Set up spaces and iBeacon listener,
        proximityManager.spaces().iBeaconRegions(BeaconRegionList.beaconRegions);
        proximityManager.setIBeaconListener(createIBeaconListener());
        proximityManager.setScanStatusListener((createSimpleScanStatusListener()));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (STOP_SERVICE_ACTION.equals(intent.getAction())) {
            stopSelf();
            return START_NOT_STICKY;
        }

        if (isRunning) {
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
        final PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        // Create stop intent with action
        final Intent intent = BeaconScanService.createIntent(this);
        intent.setAction(STOP_SERVICE_ACTION);
        final PendingIntent stopIntent = PendingIntent.getService(this, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);

        // Build notification
        final NotificationCompat.Action action = new NotificationCompat.Action(0, "Stop", stopIntent);
        final Notification notification = new NotificationCompat.Builder(this, DEFAULT_CHANNEL_ID)
                .setContentTitle("Caregiver").setContentText("Actively scanning beacons").addAction(action)
                .setSmallIcon(android.R.mipmap.sym_def_app_icon) // TODO: Add caregiver icon
                .setContentIntent(pendingIntent).build();

        // Start foreground service
        startForeground(1, notification);
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        final NotificationManager notificationManager = (NotificationManager) getSystemService(
                Context.NOTIFICATION_SERVICE);
        if (notificationManager == null)
            return;

        final NotificationChannel channel = new NotificationChannel(DEFAULT_CHANNEL_ID, NOTIFICATION_CHANEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT);
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
                Log.i("BeaconService", "Scanning started");
            }

            @Override
            public void onScanStop() {
                Log.i("BeaconService", "Scanning stopped");
            }
        };
    }


    private void sendBeaconNotification(String contentText) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(BeaconScanService.this, DEFAULT_CHANNEL_ID)
                .setSmallIcon(android.R.drawable.alert_dark_frame).setContentTitle("Beacon Discovered")
                .setContentText(contentText);

        NotificationManagerCompat notificationManager = NotificationManagerCompat.from(BeaconScanService.this);
        notificationManager.notify(2, builder.build());
    }

    private IBeaconListener createIBeaconListener() {
        return new SimpleIBeaconListener() {
            @RequiresApi(api = Build.VERSION_CODES.N)
            @Override
            public void onIBeaconDiscovered(IBeaconDevice ibeacon, IBeaconRegion region) {

                Log.i("BeaconService", "Beacon discovered " + region.getIdentifier() + " beacon address " + ibeacon.getAddress());
                if (regionRssiMap.containsKey(region.getIdentifier())) {

                    double oldSum = regionRssiMap.get(region.getIdentifier()).get("sum");
                    double oldCount = regionRssiMap.get(region.getIdentifier()).get("count");
                    double oldDistance = regionRssiMap.get(region.getIdentifier()).get("dist");


                    double newSum = oldSum + ibeacon.getRssi();
                    double newCount = oldCount + 1;
                    double newDistance = oldDistance + ibeacon.getDistance();

                    regionRssiMap.get(region.getIdentifier()).replace("sum", newSum);
                    regionRssiMap.get(region.getIdentifier()).replace("count", newCount);
                    regionRssiMap.get(region.getIdentifier()).replace("dist", newDistance);


                    if (getTimeNow() - lastTimeInMillis >= thirtySecondsInMillis) {

                        final String[] maxRegion = new String[1];
                        final double[] minDist = new double[1];
                        final double[] maxRssi = {-1000.0};

                        regionRssiMap.forEach(
                                (regionKey, rssiData) -> {
                                    if (rssiData.get("count") != 0) {

                                        double avgRssi = (double) rssiData.get("sum") / rssiData.get("count");
                                        double avgDist = (double) rssiData.get("dist") / rssiData.get("count");

                                        if (avgRssi > maxRssi[0]) {
                                            maxRssi[0] = avgRssi;
                                            maxRegion[0] = regionKey;
                                            minDist[0] = avgDist;
                                        }

                                        regionRssiMap.get(regionKey).replace("sum", 0.0);
                                        regionRssiMap.get(regionKey).replace("count", 0.0);
                                        regionRssiMap.get(regionKey).replace("dist", 0.0);

                                    }
                                });

                        Log.i("TaskNotif", "Max Region = " + maxRegion[0] + " Distance = "+ minDist[0]);

                        if ((lastSeenRegionIdentifier != null) && (!maxRegion[0].equals(lastSeenRegionIdentifier)) && (minDist[0] < distanceThreshold)) {

                            String contentText = String.format("Region = %s, Distance = %f, RSSI = %f, Timestamp = %s",
                                    maxRegion[0], minDist[0], maxRssi[0],
                                    convertUnixToTimestamp(ibeacon.getTimestamp()));
                            sendBeaconNotification(contentText);

                            sendTaskNotification(maxRegion[0]);
                            lastSeenRegionIdentifier = maxRegion[0];
                        }

                        lastTimeInMillis = getTimeNow();
                    }
                }
            }
        };
    }

    private String convertUnixToTimestamp(long unixTime) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
        Date date = new java.util.Date(unixTime);
        return dateFormatter.format(date);
    }

    private long getTimeNow() {
        return Calendar.getInstance().getTimeInMillis();
    }

    private void sendTaskNotification(String roomName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference rooms = database.child("/users/" + userId + "/rooms");

        rooms.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot roomSnapShot : snapshot.getChildren()) {
                    if (roomSnapShot.getKey().equals(roomName)) {
                        for (DataSnapshot task : roomSnapShot.child("tasks").getChildren()) {
                            String assignedStatus = (String) task.child("assignedStatus").getValue();
                            if (assignedStatus.equals("true") || assignedStatus.equals("True")) {
                                String contentText = String.format("You have tasks in the %s", roomName);
                                sendBeaconNotification(contentText);
                                return;
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TaskNotification", "sendTaskNotifications failed", error.toException());
            }
        });
    }

}
