package com.example.honeyimhome;

import android.app.Application;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.IntentFilter;
import android.os.Build;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import java.util.concurrent.TimeUnit;

public class MyAwesomeApp extends Application {

    LocalSendSmsBroadcastReceiver myBroadcastSms;
    String nameAction = "POST_PC.ACTION_SEND_SMS";
    String CHANNEL_ID = "SMS CHANNEL";

    @Override
    public void onCreate() {
        super.onCreate();
        myBroadcastSms = new LocalSendSmsBroadcastReceiver();

        // Create Notification Channel
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

            CharSequence name = getString(R.string.channel_name);
            String description = getString(R.string.channel_description);
            int importance = NotificationManager.IMPORTANCE_DEFAULT;
            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
            channel.setDescription(description);
            // Register the channel with the system; you can't change the importance
            // or other notification behaviors after this
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);

            LocalBroadcastManager.getInstance(this).registerReceiver(myBroadcastSms, new IntentFilter(nameAction));

            PeriodicWorkRequest worker = new PeriodicWorkRequest.
                    Builder(MyPeriodicWorker.class, 15, TimeUnit.MINUTES).build();

            WorkManager.getInstance(this).
                    enqueueUniquePeriodicWork("LocationWorker", ExistingPeriodicWorkPolicy.REPLACE, worker);


        }

    }
}
