package com.example.caregiver;

import android.app.Application;

import com.example.caregiver.model.Task;
import com.kontakt.sdk.android.common.KontaktSDK;

import java.util.List;

public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        KontaktSDK.initialize(this);
    }

    /* Return a list of tasks associated with that caregivee */
    public interface TaskCallback {
        void onDataReceived(List<Task> tasks);
    }


    /* Return a list of rooms associated with that caregivee */
    public interface RoomCallback {
        void onDataReceived(List<String> rooms);
    }
}

