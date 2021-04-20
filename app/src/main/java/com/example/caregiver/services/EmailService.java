package com.example.caregiver.services;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.IBinder;

import androidx.annotation.Nullable;

public class EmailService extends Service {

    private Context context;

    public EmailService(Context context)
    {
        this.context = context;
    }

    public void sendEmail(String[] to, String subject, String body)
    {
        Intent emailIntent = new Intent(Intent.ACTION_SENDTO);
        emailIntent.setData(Uri.parse("mailto:"/* + to*/));
        emailIntent.putExtra(Intent.EXTRA_EMAIL, to);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        emailIntent.putExtra(Intent.EXTRA_TEXT, body);

        try
        {
            context.startActivity(emailIntent);
        }
        catch (Exception e)
        {
            System.out.println(e.getMessage());
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
