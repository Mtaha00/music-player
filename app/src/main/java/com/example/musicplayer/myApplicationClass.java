package com.example.musicplayer;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;

public class myApplicationClass extends Application {
    public static final String CHANNEL_ID="channelID";
    private static final String CHANNEL_NAME="music";



    @Override
    public void onCreate() {
        super.onCreate();

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationManager manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

            NotificationChannel notificationChannel=new NotificationChannel(CHANNEL_ID,CHANNEL_NAME,NotificationManager.IMPORTANCE_HIGH);
            manager.createNotificationChannel(notificationChannel);
        }

    }
}
