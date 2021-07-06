package org.studip.unofficial_app.ui;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.ShortcutManagerCompat;

import org.studip.unofficial_app.databinding.ActivityShareBinding;

public class ShareActivity extends AppCompatActivity
{
    public static final String SHARE_EXTRA = "org.studip.unofficial_app.share";
    public static final String SHARE_FORUM = "forum";
    public static final String SHARE_MESSAGE = "message";
    public static final String SHARE_BLUBBER = "blubber";
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        ActivityShareBinding binding = ActivityShareBinding.inflate(getLayoutInflater());
        
        Intent i = getIntent().getParcelableExtra(Intent.EXTRA_INTENT);
        if (i == null) {
            finish();
        } else {
            String shortcut = i.getStringExtra(ShortcutManagerCompat.EXTRA_SHORTCUT_ID);
            if (shortcut != null) {
                String[] split = shortcut.split(":");
                if (split.length == 2) {
                    String type = split[0];
                    if ("message".equals(type)) {
                        toHomeActivity(i, SHARE_MESSAGE);
                    }
                }
                finishAndRemoveTask();
            }
            
            binding.shareBlubber.setOnClickListener(v -> toHomeActivity(i, SHARE_BLUBBER));
            binding.shareForum.setOnClickListener(v -> toHomeActivity(i, SHARE_FORUM));
            binding.shareMessage.setOnClickListener(v -> toHomeActivity(i, SHARE_MESSAGE));
        }
        
        setContentView(binding.getRoot());
    }
    
    private void toHomeActivity(Intent original, String share) {
        Intent i = new Intent(this, HomeActivity.class);
        i.fillIn(original, 0);
        i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        i.putExtra(SHARE_EXTRA, share);
        finishAndRemoveTask();
        
        if (! startInHomeTask(i)) {
            //System.out.println("no Task found");
            Intent h = new Intent(this, HomeActivity.class);
            h.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            h.setAction(Intent.ACTION_MAIN);
            h.putExtra(Intent.EXTRA_INTENT, i);
            startActivity(h);
        }
    }
    
    private boolean startInHomeTask(Intent i) {
        ActivityManager m = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.AppTask t : m.getAppTasks()) {
            if (Intent.ACTION_MAIN.equals(t.getTaskInfo().baseIntent.getAction())) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (! t.getTaskInfo().topActivity.equals(ComponentName.createRelative(this, ".ui.HomeActivity"))) {
                        t.finishAndRemoveTask();
                        return false;
                    }
                } else {
                    t.finishAndRemoveTask();
                    return false;
                }
                t.startActivity(this, i, null);
                return true;
            }
        }
        return false;
    }
    
}




