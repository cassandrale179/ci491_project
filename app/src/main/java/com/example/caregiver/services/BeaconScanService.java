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

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;

public class BeaconScanService extends Service {

    private static final String STOP_SERVICE_ACTION = "STOP_SERVICE_ACTION";
    private static final CharSequence NOTIFICATION_CHANEL_NAME = "Caregiver channel name";
    private ProximityManager proximityManager;
    private boolean isRunning; // Flag indicating if service is already running.
    private static final String DEFAULT_CHANNEL_ID = "Caregiver_Channel_ID";
    private Intent serviceIntent;
    private static String lastSeenRegionIdentifier = null;
    private static HashMap<String, HashMap<String, Integer>> regionRssiMap = new HashMap<String, HashMap<String, Integer>>();
    private static HashMap<String, Integer> rssiDataMap = new HashMap<String, Integer>();

    static {
        rssiDataMap.put("sum", 0);
        rssiDataMap.put("count", 0);
    }

    ;
    private long lastTimeInMillis = getTimeNow();
    private static int thirtySecondsInMillis = 30000;

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
                // Scans for 10 minutes with 2 second pauses to avoid getting killed by Android
                .scanPeriod(ScanPeriod.create(TimeUnit.MINUTES.toMillis(10), TimeUnit.SECONDS.toMillis(2)))
                // Using BALANCED for best performance/battery ratio
                .scanMode(ScanMode.BALANCED);

        // Setting up iBeacon and Eddystone spaces listeners
        // proximityManager.setSpaceListener(createSpaceListener());

        // Set up iBeacon listener
        proximityManager.setIBeaconListener(createIBeaconListener());
    }

    // TODO: When user sets up the regions, send UUID and region as input here
    private void setupSpaces() {
        Collection<IBeaconRegion> beaconRegions = new ArrayList<>();
        // Setting up single iBeacon region. Put your own desired values here.
        IBeaconRegion region1 = new BeaconRegion.Builder().identifier("Mannika Bedroom")
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) // TODO: Add users UUID
                // Optional major and minor values
                .major(1).minor(2).build();
        beaconRegions.add(region1);
        regionRssiMap.put("Mannika Bedroom", rssiDataMap);

        IBeaconRegion region2 = new BeaconRegion.Builder().identifier("Mannika Bathroom") // Region identifier is
                // mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) // TODO: Add users UUID
                // Optional major and minor values
                .major(1).minor(1).build();
        beaconRegions.add(region2);
        regionRssiMap.put("Mannika Bathroom", rssiDataMap);

        IBeaconRegion region3 = new BeaconRegion.Builder().identifier("Mannika Kitchen") // Region identifier is
                // mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) // TODO: Add users UUID
                // Optional major and minor values
                .major(1).minor(3).build();
        beaconRegions.add(region3);
        regionRssiMap.put("Mannika Kitchen", rssiDataMap);

        IBeaconRegion region4 = new BeaconRegion.Builder().identifier("Jui Bedroom")
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) // TODO: Add users UUID
                // Optional major and minor values
                .major(2).minor(2).build();
        beaconRegions.add(region4);
        regionRssiMap.put("Jui Bedroom", rssiDataMap);

        IBeaconRegion region5 = new BeaconRegion.Builder().identifier("Jui Bathroom") // Region identifier is mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) // TODO: Add users UUID
                // Optional major and minor values
                .major(2).minor(1).build();
        beaconRegions.add(region5);
        regionRssiMap.put("Jui Bathroom", rssiDataMap);

        IBeaconRegion region6 = new BeaconRegion.Builder().identifier("Jui Kitchen") // Region identifier is mandatory.
                .proximity(UUID.fromString("f7826da6-4fa2-4e98-8024-bc5b71e0893e")) // TODO: Add users UUID
                // Optional major and minor values
                .major(2).minor(3).build();
        regionRssiMap.put("Jui Kitchen", rssiDataMap);

        beaconRegions.add(region6);

        proximityManager.spaces().iBeaconRegions(beaconRegions);
    }

    // private SpaceListener createSpaceListener() {
    // Log.i("Sample", "createSpaceListener Called");
    // return new SimpleSpaceListener() {
    // @Override
    // public void onRegionEntered(IBeaconRegion region) {
    // Log.i("Sample", "Region Entered");
    // NotificationCompat.Builder builder = new
    // NotificationCompat.Builder(BeaconScanService.this, DEFAULT_CHANNEL_ID)
    // .setSmallIcon(android.R.drawable.alert_dark_frame)
    // .setContentTitle("Region Entered")
    // .setContentText(region.getIdentifier());

    // NotificationManagerCompat notificationManager =
    // NotificationManagerCompat.from(BeaconScanService.this);
    // notificationManager.notify(0, builder.build());
    // }

    // @Override
    // public void onRegionAbandoned(IBeaconRegion region) {
    // Log.i("Sample", "Region Abandoned");
    // NotificationCompat.Builder builder = new
    // NotificationCompat.Builder(BeaconScanService.this, DEFAULT_CHANNEL_ID)
    // .setSmallIcon(android.R.drawable.alert_dark_frame)
    // .setContentTitle("Region Abandoned")
    // .setContentText(region.getIdentifier());

    // NotificationManagerCompat notificationManager =
    // NotificationManagerCompat.from(BeaconScanService.this);
    // notificationManager.notify(0, builder.build());
    // }
    // };
    // }

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
                Log.i("Sample", "Scanning started");
            }

            @Override
            public void onScanStop() {
                Log.i("Sample", "Scanning stopped");
            }
        };
    }

    // lastTimestamp = 0
    // onIBeaconDiscovered()
    // check if currTimestamp - lastTimestamp < 30
    // write to container
    // else:
    // Average RSSI for each region
    // Send notification if curr region != last seen region
    // empty container
    // lastTimestamp = currTimestamp

    private void sendBeaconNotification(String contentText) {
        Log.i("Sample", "IBeacon discovered: " + contentText);
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

                if (getTimeNow() - lastTimeInMillis < thirtySecondsInMillis) {
                    int oldSum = regionRssiMap.get(region.getIdentifier()).get("sum");
                    int oldCount = regionRssiMap.get(region.getIdentifier()).get("count");
                    regionRssiMap.get(region.getIdentifier()).replace("sum", oldSum + ibeacon.getRssi());
                    regionRssiMap.get(region.getIdentifier()).replace("count", oldCount + 1);
                } else {
                    final String[] maxRegion = new String[1];
                    int maxRssi = Integer.MIN_VALUE;
                    regionRssiMap.forEach(
                            (regionKey, rssiData) -> {
                                if (rssiDataMap.get("count") != 0) {
                                    float avg = rssiDataMap.get("sum") / rssiDataMap.get("count");
                                    if (avg > maxRssi) {
                                        maxRegion[0] = regionKey;
                                    }
                                    rssiDataMap.replace("sum", 0);
                                    rssiDataMap.replace("count", 0);
                                    if (maxRegion[0] != lastSeenRegionIdentifier) {
                                        String contentText = String.format("Region = %s, Distance = %f, RSSI = %d, Timestamp = %s",
                                                region.getIdentifier(), ibeacon.getDistance(), ibeacon.getRssi(),
                                                convertUnixToTimestamp(ibeacon.getTimestamp()));
                                        sendBeaconNotification(contentText);
                                        lastSeenRegionIdentifier = maxRegion[0];
                                        lastTimeInMillis = getTimeNow();
                                    }
                                }
                            });
                }
                // if (ibeacon.getDistance() <= 1.0) {
                //     lastSeenRegionIdentifier = region.getIdentifier();
                //     String contentText = String.format("Region = %s, Distance = %f, RSSI = %d, Timestamp = %s",
                //             region.getIdentifier(), ibeacon.getDistance(), ibeacon.getRssi(),
                //             convertUnixToTimestamp(ibeacon.getTimestamp()));

                //     sendBeaconNotification(contentText);
                // }
            }

            // @Override
            // public void onIBeaconLost(IBeaconDevice ibeacon, IBeaconRegion region) {
            // super.onIBeaconLost(ibeacon, region);
            // if (ibeacon.getDistance() > 1.5) {
            // String contentText = String.format("Region = %s, Distance = %f, RSSI = %d,
            // Timestamp = %s", region.getIdentifier(), ibeacon.getDistance(),
            // ibeacon.getRssi(), convertUnixToTimestamp(ibeacon.getTimestamp()));
            // Log.e("Sample", "IBeacon Lost: " + contentText);
            // NotificationCompat.Builder builder = new
            // NotificationCompat.Builder(BeaconScanService.this, DEFAULT_CHANNEL_ID)
            // .setSmallIcon(android.R.drawable.alert_dark_frame)
            // .setContentTitle("Beacon Lost")
            // .setContentText(contentText);

            // NotificationManagerCompat notificationManager =
            // NotificationManagerCompat.from(BeaconScanService.this);
            // notificationManager.notify(3, builder.build());
            // }
            // }
        };
    }

    private String convertUnixToTimestamp(long unixTime) {
        SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
        Date date = new java.util.Date(unixTime);
        return dateFormatter.format(date);
    }

    private long getTimeNow() {
        // SimpleDateFormat dateFormatter = new SimpleDateFormat("yyyy:MM:dd_HH:mm:ss");
        return Calendar.getInstance().getTimeInMillis();
    }
}
