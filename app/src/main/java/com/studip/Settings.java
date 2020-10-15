package com.studip;


import android.content.SharedPreferences;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.security.crypto.EncryptedSharedPreferences;

public class Settings
{
    public static final int AUTHENTICATION_BASIC = 1;
    public static final int AUTHENTICATION_COOKIE = 2;
    public static final int AUTHENTICATION_OAUTH = 3;
    
    
    public static final int authentification_method_default = AUTHENTICATION_BASIC;
    private static final String authentification_method_key = "authentification_method";
    public volatile int authentification_method;
    
    private static final String theme_key = "theme";
    public volatile int theme;
    
    private Settings() {}
    public static Settings load(SharedPreferences prefs)
    {
        Settings s = new Settings();
        
        if (prefs instanceof EncryptedSharedPreferences)
        {
            s.authentification_method = prefs.getInt(authentification_method_key,authentification_method_default);
            s.theme = prefs.getInt(theme_key,AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        }
        else
        {
            s.authentification_method = authentification_method_default;
            s.theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        return s;
    }
    
    
    public void safe(SharedPreferences prefs)
    {
        if (prefs instanceof  EncryptedSharedPreferences)
        {
            SharedPreferences.Editor e = prefs.edit();
            e.putInt(authentification_method_key,authentification_method);
            e.putInt(theme_key,theme);
            
            
            e.apply();
        }
        else
        {
            System.out.println("not saving settings as shared preferences aren't encrypted");
        }
    }
    
    
}
