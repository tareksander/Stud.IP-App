package com.studip;


import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.security.crypto.EncryptedSharedPreferences;

public class Settings
{
    public static final int AUTHENTICATION_BASIC = 1;
    public static final int AUTHENTICATION_COOKIE = 2;
    public static final int AUTHENTICATION_OAUTH = 3;
    
    
    public static final int authentication_method_default = AUTHENTICATION_COOKIE;
    private static final String authentication_method_key = "authentification_method";
    public volatile int authentication_method;
    
    private static final String theme_key = "theme";
    public volatile int theme;
    
    
    private static final String notification_period_key = "notification_period";
    public volatile int notification_period;
    
    
    private static final String notification_service_enabled_key = "notification_service_enabled";
    public volatile boolean notification_service_enabled;
    
    private Settings() {}
    public static Settings load(SharedPreferences prefs)
    {
        Settings s = new Settings();
        
        if (prefs instanceof EncryptedSharedPreferences)
        {
            s.authentication_method = prefs.getInt(authentication_method_key,authentication_method_default);
            s.theme = prefs.getInt(theme_key,AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
            s.notification_period = prefs.getInt(notification_period_key,30);
            s.notification_service_enabled = prefs.getBoolean(notification_service_enabled_key,false);
        }
        else
        {
            s.authentication_method = authentication_method_default;
            s.theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
            s.notification_period = 30;
            s.notification_service_enabled = false;
        }
        return s;
    }
    
    
    public void safe(SharedPreferences prefs)
    {
        if (prefs instanceof  EncryptedSharedPreferences)
        {
            SharedPreferences.Editor e = prefs.edit();
            e.putInt(authentication_method_key,authentication_method);
            e.putInt(theme_key,theme);
            e.putInt(notification_period_key,notification_period);
            e.putBoolean(notification_service_enabled_key,notification_service_enabled);
            
            
            e.apply();
        }
        else
        {
            System.out.println("not saving settings as shared preferences aren't encrypted");
        }
    }
    
    
}
