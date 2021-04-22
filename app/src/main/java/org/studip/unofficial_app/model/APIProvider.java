package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.studip.unofficial_app.api.API;

public class APIProvider
{
    static private API api;
    public static API getAPI(Context c) {
        if (api == null) {
            api = loadAPI(c);
            if (api == null) {
                return null;
            }
        }
        return api;
    }
    public static EncryptedSharedPreferences getPrefs(Context c) {
        try
        {
            MasterKey.Builder b = new MasterKey.Builder(c);
            b.setKeyScheme(MasterKey.KeyScheme.AES256_GCM);
            MasterKey m = b.build();
            return (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    c,
                    "api",
                    m,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (Exception ignored) {}
        return null;
    }
    public static API loadAPI(Context c) {
        try
        {
            EncryptedSharedPreferences prefs = getPrefs(c);
            if (prefs != null)
            {
                return API.load(prefs);
            }
        } catch (Exception ignored) {}
        return null;
    }
    public static API newAPI(String hostname) {
        api = new API(hostname);
        return api;
    }
}
