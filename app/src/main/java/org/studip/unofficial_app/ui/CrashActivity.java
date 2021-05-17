package org.studip.unofficial_app.ui;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.security.crypto.EncryptedSharedPreferences;

import org.studip.unofficial_app.App;
import org.studip.unofficial_app.databinding.ActivityCrashBinding;
import org.studip.unofficial_app.model.SettingsProvider;

import java.util.Date;

public class CrashActivity extends AppCompatActivity
{
    
    public static boolean CRASHED = false;
    private ActivityCrashBinding binding;
    private String trace;
    private Date timestamp;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        CRASHED = true;
        Intent i = getIntent();
        trace = i.getStringExtra("Backtrace");
        if (trace == null) {
            System.out.println("no backtrace found");
            finish();
            return;
        }
        trace = "Android API Version: " + Build.VERSION.SDK_INT + "\n" + trace;
        System.err.println(trace);
        System.out.println("crash activity");
        
        timestamp = new Date();
        
        
        binding = ActivityCrashBinding.inflate(getLayoutInflater());
        
        
        binding.crashTrace.setText(trace);
        
        
        setContentView(binding.getRoot());
    }
    
    
    
    public void onClipboard(View v) {
        ClipboardManager m = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        m.setPrimaryClip(ClipData.newPlainText("Crash log",trace));
    }

    public void onMail(View v) {
        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType("text/plain");
        i.putExtra(Intent.EXTRA_EMAIL,"unofficial_studip_app_bugs@web.de");
        EncryptedSharedPreferences prefs = SettingsProvider.getSettingsPreferences(this);
        String uuid = "default";
        if (prefs != null) {
            uuid = prefs.getString(App.APP_UUID_KEY, "default");
        }
        i.putExtra(Intent.EXTRA_SUBJECT,"Crash from "+uuid+" at "+timestamp.toString());
        i.putExtra(Intent.EXTRA_TEXT,trace);
        startActivity(Intent.createChooser(i,""));
    }

    public void onGithub(View v) {
        ClipboardManager m = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        m.setPrimaryClip(ClipData.newPlainText("Crash log",trace));
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        i.setData(Uri.parse("https://github.com/tareksander/Stud.IP-App/issues"));
        startActivity(i);
    }

    public void onRestart(View v) {
        Intent i = new Intent(getApplicationContext(), HomeActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        System.exit(0);
    }
    
}