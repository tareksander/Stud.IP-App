package com.studip;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class CacheService extends Service
{
    @Override
    public void onCreate()
    {
        super.onCreate();
    }
    
    
    // TODO this service should periodically save the web data in Data, and can be asked whether there is saved data
    // to reduce network usage
    
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        
        
        return Service.START_NOT_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // no need to bind, as the service is only used in this application and all data is accessible via Data
        return null;
    }
}