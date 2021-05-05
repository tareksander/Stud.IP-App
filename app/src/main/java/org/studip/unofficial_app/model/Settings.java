package org.studip.unofficial_app.model;


import android.annotation.SuppressLint;
import android.app.Notification;
import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.NotificationCompat;
import androidx.security.crypto.EncryptedSharedPreferences;

import org.studip.unofficial_app.api.rest.StudipNotifications;

public class Settings
{
    public static final int AUTHENTICATION_BASIC = 1;
    public static final int AUTHENTICATION_COOKIE = 2;
    public static final int AUTHENTICATION_OAUTH = 3;
    
    public static final String LOGOUT_KEY = "logout";
    public volatile boolean logout;
    
    public static final int authentication_method_default = AUTHENTICATION_COOKIE;
    private static final String authentication_method_key = "authentification_method";
    public volatile int authentication_method;
    
    private static final String theme_key = "theme";
    public volatile int theme;
    
    
    private static final String notification_period_key = "notification_period";
    public volatile int notification_period;
    
    
    private static final String notification_service_enabled_key = "notification_service_enabled";
    public volatile boolean notification_service_enabled;
    
    
    // The API 25- notification priorities
    public int forum_priority;
    public int files_priority;
    public int messages_priority;
    public int other_priority;
    
    
    private static final String NOTIFICATION_ID_KEY = "last_notification";
    public long last_notification_id;
    
    
    private static final String NOTIFICATION_VISIBILITY_KEY = "notification_visibility";
    
    public int notification_visibility;
    
    public Settings()
    {
        defaults();
    }
    private void defaults() {
        logout = false;
        authentication_method = authentication_method_default;
        theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        notification_period = 30;
        notification_service_enabled = false;
        
        forum_priority = NotificationCompat.PRIORITY_DEFAULT;
        files_priority = NotificationCompat.PRIORITY_LOW;
        messages_priority = NotificationCompat.PRIORITY_HIGH;
        other_priority = NotificationCompat.PRIORITY_LOW;
        
        last_notification_id = 0;
        notification_visibility = NotificationCompat.VISIBILITY_SECRET;
    }
    public static Settings load(SharedPreferences prefs)
    {
        Settings s = new Settings();
        
        if (prefs instanceof EncryptedSharedPreferences)
        {
            s.logout = prefs.getBoolean(LOGOUT_KEY, false);
            s.authentication_method = prefs.getInt(authentication_method_key, authentication_method_default);
            s.theme = prefs.getInt(theme_key, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            s.notification_period = prefs.getInt(notification_period_key, 30);
            s.notification_service_enabled = prefs.getBoolean(notification_service_enabled_key, false);
            
            s.forum_priority = prefs.getInt(Notifications.CHANNEL_FORUM, NotificationCompat.PRIORITY_DEFAULT);
            s.files_priority = prefs.getInt(Notifications.CHANNEL_FILES, NotificationCompat.PRIORITY_LOW);
            s.messages_priority = prefs.getInt(Notifications.CHANNEL_MESSAGES, NotificationCompat.PRIORITY_HIGH);
            s.other_priority = prefs.getInt(Notifications.CHANNEL_OTHER, NotificationCompat.PRIORITY_LOW);
            
            s.last_notification_id = prefs.getLong(NOTIFICATION_ID_KEY, 0);
            s.notification_visibility = prefs.getInt(NOTIFICATION_VISIBILITY_KEY,NotificationCompat.VISIBILITY_SECRET);
        }
        else
        {
            s.defaults();
        }
        return s;
    }
    
    
    @SuppressLint("ApplySharedPref")
    public void safe(SharedPreferences prefs)
    {
        if (prefs instanceof  EncryptedSharedPreferences)
        {
            SharedPreferences.Editor e = prefs.edit();
            e.putBoolean(LOGOUT_KEY, logout);
            e.putInt(authentication_method_key, authentication_method);
            e.putInt(theme_key, theme);
            e.putInt(notification_period_key, notification_period);
            e.putBoolean(notification_service_enabled_key, notification_service_enabled);
            
            e.putInt(Notifications.CHANNEL_FORUM, forum_priority);
            e.putInt(Notifications.CHANNEL_FILES, files_priority);
            e.putInt(Notifications.CHANNEL_MESSAGES, messages_priority);
            e.putInt(Notifications.CHANNEL_OTHER, other_priority);
            
            e.putLong(NOTIFICATION_ID_KEY, last_notification_id);
            e.putInt(NOTIFICATION_VISIBILITY_KEY, notification_visibility);
            
            e.commit();
        }
        else
        {
            System.out.println("not saving settings as shared preferences aren't encrypted");
        }
    }
    
    
}
