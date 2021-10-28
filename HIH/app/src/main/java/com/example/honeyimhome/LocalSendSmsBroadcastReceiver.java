package com.example.honeyimhome;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import java.util.Random;

public class LocalSendSmsBroadcastReceiver extends BroadcastReceiver {

    String CHANNEL_ID = "SMS CHANNEL";

    @Override
    public void onReceive(Context context, Intent intent) {

        if(ActivityCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED){
            // Need more permissions ? Write ?

            String action = intent.getAction();
            if(action !=  null && action.equals("POST_PC.ACTION_SEND_SMS")){

                String phoneNumber = intent.getStringExtra("phoneNumber");
                phoneNumber = phoneNumber.substring(1,11);
                String content = intent.getStringExtra("content");

                if(phoneNumber == null || content == null){
                    String tag = "ERROR";
                    Log.d(tag,  "NO PHONE OR NO CONTENT");
                    return;
                }

                SmsManager smsManager = SmsManager.getDefault();
                smsManager.sendTextMessage(phoneNumber, null, content,null, null);

//                Toast.makeText(context, "SMS sent.",Toast.LENGTH_LONG).show();
                String toSend ="Sending sms to " + phoneNumber + " : " + content;


                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    CharSequence name = CHANNEL_ID;
                    String description = CHANNEL_ID;
                    int importance = NotificationManager.IMPORTANCE_DEFAULT;
                    NotificationChannel channel = new NotificationChannel(CHANNEL_ID, name, importance);
                    channel.setDescription(description);
                    // Register the channel with the system; you can't change the importance
                    // or other notification behaviors after this
                    NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
                    notificationManager.createNotificationChannel(channel);

//                    NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
// The id of the channel.
//                    notificationManager.deleteNotificationChannel(channel);

                    Notification ntfc = new NotificationCompat.Builder(context, CHANNEL_ID)
                            .setSmallIcon(R.drawable.notification_icon)
                            .setContentTitle(toSend)
                            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                            .build();
//
                    Random r = new Random();
                    int notificationId = r.nextInt();
//
                    NotificationManagerCompat.from(context).notify(notificationId, ntfc);
                }

            }

        }

        else{

            String tag = "ERROR";
            Log.d(tag, "ERROR PERMISSION");
        }
    }


}
