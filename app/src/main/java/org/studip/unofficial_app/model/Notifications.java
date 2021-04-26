package org.studip.unofficial_app.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.core.app.NotificationManagerCompat;

public class Notifications
{
    public static final String CHANNEL_DOWNLOADS = "downloads";
    
    public static void initChannels(Context c) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManagerCompat n = NotificationManagerCompat.from(c);
            NotificationChannel down = new NotificationChannel(CHANNEL_DOWNLOADS,"Downloads", NotificationManager.IMPORTANCE_LOW);
            n.createNotificationChannel(down);

        }
    }
    
}
