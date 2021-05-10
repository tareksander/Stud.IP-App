package org.studip.unofficial_app.model;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;

import org.studip.unofficial_app.R;

public class Notifications
{
    public static final String CHANNEL_FORUM = "channel_forum";
    public static final String CHANNEL_FILES = "channel_files";
    public static final String CHANNEL_MESSAGES = "channel_messages";
    public static final String CHANNEL_OTHER = "channel_other";
    
    public static void initChannels(Context c) {
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationManagerCompat n = NotificationManagerCompat.from(c);
            NotificationChannel forum = new NotificationChannel(CHANNEL_FORUM,c.getString(R.string.channel_forum), NotificationManager.IMPORTANCE_DEFAULT);
            n.createNotificationChannel(forum);
            
            NotificationChannel files = new NotificationChannel(CHANNEL_FILES,c.getString(R.string.channel_files),NotificationManager.IMPORTANCE_DEFAULT);
            n.createNotificationChannel(files);
    
            NotificationChannel messages = new NotificationChannel(CHANNEL_MESSAGES,c.getString(R.string.channel_messages),NotificationManager.IMPORTANCE_HIGH);
            n.createNotificationChannel(messages);
    
            NotificationChannel other = new NotificationChannel(CHANNEL_OTHER,c.getString(R.string.channel_other),NotificationManager.IMPORTANCE_LOW);
            n.createNotificationChannel(other);
        }
    }
    
    /** Sets the importance/Channel according to the notification type.
     * @param c The Context of the notification
     * @param b The notification
     * @param type Has to be one of the CHANNEL_* constants
     */
    public static void setType(@NonNull Context c, @NonNull NotificationCompat.Builder b, @NonNull String type) {
        b.setGroup(type);
        if (Build.VERSION.SDK_INT >= 26) {
            b.setChannelId(type);
        } else {
            int priority = NotificationCompat.PRIORITY_DEFAULT;
            b.setPriority(NotificationCompat.PRIORITY_DEFAULT);
            Settings s = SettingsProvider.getSettings(c);
            if (s != null) {
                if (CHANNEL_FORUM.equals(type)) {
                    priority = s.forum_priority;
                }
                if (CHANNEL_FILES.equals(type)) {
                    priority = s.files_priority;
                }
                if (CHANNEL_MESSAGES.equals(type)) {
                    //System.out.println(s.messages_priority);
                    priority = s.messages_priority;
                }
                if (CHANNEL_OTHER.equals(type)) {
                    priority = s.other_priority;
                }
            }
            b.setPriority(priority);
            if (priority == NotificationCompat.PRIORITY_DEFAULT || priority == NotificationCompat.PRIORITY_HIGH) {
                b.setDefaults(NotificationCompat.DEFAULT_SOUND);
            }
            
            
        }
    }
    
}
