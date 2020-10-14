package com.studip;


import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;

public class Settings
{
    public static final int AUTHENTICATION_BASIC = 1;
    public static final int AUTHENTICATION_COOKIE = 2;
    public static final int AUTHENTICATION_OAUTH = 3;
    
    
    public static final int authentification_method_default = AUTHENTICATION_BASIC;
    public static final String authentification_method_key = "authentification_method";
    public volatile int authentification_method;
    
    
    private Settings() {}
    public static Settings load(SharedPreferences prefs)
    {
        Settings s = new Settings();
        
        if (prefs instanceof EncryptedSharedPreferences)
        {
            s.authentification_method = prefs.getInt(authentification_method_key,authentification_method_default);
        }
        else
        {
            s.authentification_method = authentification_method_default;
        }
        return s;
    }
    
    
    public void safe(SharedPreferences prefs)
    {
        if (prefs instanceof  EncryptedSharedPreferences)
        {
            SharedPreferences.Editor e = prefs.edit();
            e.putInt(authentification_method_key,authentification_method);
            
            
            
            e.apply();
        }
        else
        {
            System.out.println("not saving settings as shared preferences aren't encrypted");
        }
    }
    
    
}
