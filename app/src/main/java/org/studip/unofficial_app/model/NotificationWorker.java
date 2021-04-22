package org.studip.unofficial_app.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

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
        
        
        return Result.success();
    }
}
