package org.studip.unofficial_app.model;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

public class SettingsProvider
{
    static private Settings settings;
    public static Settings getSettings(Context c) {
        if (settings == null) {
            EncryptedSharedPreferences prefs = getSettingsPreferences(c);
            if (prefs != null) {
                settings = Settings.load(prefs);
            } else {
                settings = new Settings();
            }
        }
        return settings;
    }
    public static EncryptedSharedPreferences getSettingsPreferences(Context c) {
        try
        {
            MasterKey.Builder b = new MasterKey.Builder(c);
            b.setKeyScheme(MasterKey.KeyScheme.AES256_GCM);
            MasterKey m = b.build();
            return (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    c,
                    "settings",
                    m,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
