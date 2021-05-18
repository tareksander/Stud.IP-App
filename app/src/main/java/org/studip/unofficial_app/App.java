package org.studip.unofficial_app;

import android.app.Application;
import android.content.Intent;

import androidx.security.crypto.EncryptedSharedPreferences;

import org.studip.unofficial_app.model.Notifications;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.ui.CrashActivity;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.UUID;

public class App extends Application
{
    public static final String APP_UUID_KEY = "UUID";
    @Override
    public void onCreate()
    {
        super.onCreate();
        EncryptedSharedPreferences prefs = SettingsProvider.getSettingsPreferences(this);
        if (prefs != null) {
            if (! prefs.contains(APP_UUID_KEY)) {
                prefs.edit().putString(APP_UUID_KEY, UUID.randomUUID().toString()).apply();
            }
        }
        Thread.setDefaultUncaughtExceptionHandler((t, e) ->
        {
            System.out.println("crash detected");
            if (! CrashActivity.CRASHED) {
                Intent i = new Intent(getApplicationContext(), CrashActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                StringWriter w = new StringWriter();
                e.printStackTrace(new PrintWriter(w));
                e.printStackTrace();
                i.putExtra("Backtrace", w.toString());
                startActivity(i);
            }
            System.exit(0);
        });
        // for API 26+
        Notifications.initChannels(this);
    }
}
