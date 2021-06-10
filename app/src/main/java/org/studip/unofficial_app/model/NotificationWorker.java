package org.studip.unofficial_app.model;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jsoup.Jsoup;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipNotifications;
import org.studip.unofficial_app.ui.DeepLinkActivity;

import java.util.concurrent.TimeUnit;

public class NotificationWorker extends Worker
{
    public static final String WORKER_ID = "notification";
    public static void enqueue(Context c) {
        WorkManager.getInstance(c).enqueueUniquePeriodicWork(WORKER_ID, ExistingPeriodicWorkPolicy.REPLACE,
                new PeriodicWorkRequest.Builder(NotificationWorker.class,SettingsProvider.getSettings(c).notification_period, TimeUnit.MINUTES).build());
    }
    public NotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams)
    {
        super(context, workerParams);
    }
    @NonNull
    @Override
    public Result doWork()
    {
        //System.out.println("checking for notifications");
        API api = APIProvider.getAPI(getApplicationContext());
        Settings s = SettingsProvider.getSettings(getApplicationContext());
        if (api != null) {
            //s.last_notification_id = 0;
            try {
                StudipNotifications nots = api.dispatch.getNotifications().execute().body();
                if (nots != null && nots.notifications != null) {
                    NotificationManagerCompat m = NotificationManagerCompat.from(getApplicationContext());
                    for (StudipNotifications.Notification n : nots.notifications) {
                        if (s.last_notification_id >= n.personal_notification_id) {
                            continue;
                        }
                        s.last_notification_id = n.personal_notification_id;
                        String channel = Notifications.CHANNEL_OTHER;
                        int res = R.drawable.seminar_blue;
                        if (n.url.contains("/plugins.php/opencast")) {
                            // TODO notification category for opencast
                            res = R.drawable.opencast_blue;
                        }
                        if (n.url.contains("files")) {
                            channel = Notifications.CHANNEL_FILES;
                            res = R.drawable.file_blue;
                        }
                        if (n.url.contains("forum")) {
                            channel = Notifications.CHANNEL_FORUM;
                            res = R.drawable.forum_blue;
                        }
                        if (n.url.contains("messages")) {
                            channel = Notifications.CHANNEL_MESSAGES;
                            res = R.drawable.mail_blue;
                        }
                        NotificationCompat.Builder b = new NotificationCompat.Builder(getApplicationContext(),channel);
                        Notifications.setType(getApplicationContext(),b,channel);
                        b.setSmallIcon(res);
                        b.setContentTitle(n.text);
                        b.setTicker(n.text);
                        String text = Jsoup.parse(n.html).text();
                        b.setContentText(text);
                        b.setStyle(new NotificationCompat.BigTextStyle().bigText(text));
                        int flags = PendingIntent.FLAG_ONE_SHOT;
                        if (Build.VERSION.SDK_INT >= 23) {
                            flags |= PendingIntent.FLAG_IMMUTABLE;
                        }
                        
                        Intent i = new Intent(getApplicationContext(), DeepLinkActivity.class);
                        i.setAction(Intent.ACTION_VIEW);
                        i.setData(Uri.parse(n.url));
                        //System.out.println(n.url);
                        i.putExtra(getApplicationContext().getPackageName()+".notification_id",n.personal_notification_id);
                        b.setContentIntent(PendingIntent.getActivity(getApplicationContext(),0,i,flags));
                        
                        b.setVisibility(s.notification_visibility);
                        
                        m.notify((int) (n.personal_notification_id % Integer.MAX_VALUE),b.build());
                    }
                }
            }
            catch (Exception ignored) {}
            //System.out.println(s.last_notification_id);
            s.safe(SettingsProvider.getSettingsPreferences(getApplicationContext()));
        }
        return Result.success();
    }
}
