package com.example.caregiver;

import android.content.Context;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import com.example.caregiver.services.BeaconScanService;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;


public class TaskNotification{
    private static final String DEFAULT_CHANNEL_ID = "Caregiver_Channel_ID";

    public boolean doesRoomHavePendingTasks(String roomName) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        String userId = user.getUid();
        final DatabaseReference database = FirebaseDatabase.getInstance().getReference();
        final DatabaseReference rooms = database.child("/users/" + userId + "/rooms");
        final boolean[] isAnyTaskPending = {false};


        ValueEventListener valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                for (DataSnapshot roomSnapShot : snapshot.getChildren()) {
                    if (roomSnapShot.getKey().equals(roomName)) {
                        for (DataSnapshot task : roomSnapShot.child("tasks").getChildren()) {
                            if (
                                    task.child("assignedStatus").getValue().equals(true)
                                            || task.child("assignedStatus").getValue().equals("true")
                                            || task.child("assignedStatus").getValue().equals("True")) {
                                isAnyTaskPending[0] = true;
                                return;
                            }
                        }
                    }
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TaskNotification", "doesRoomHavePendingTasks failed", error.toException());
            }
        };
        return isAnyTaskPending[0];
    }
}
