package org.studip.unofficial_app.ui.plugins;

import android.app.PendingIntent;
import android.app.PictureInPictureParams;
import android.app.RemoteAction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Rational;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.databinding.ActivityMeetingsBinding;
import org.studip.unofficial_app.ui.plugins.fragments.MeetingsFragment;

import java.util.LinkedList;

public class MeetingsActivity extends AppCompatActivity
{
    public static final String ACTION_LOGOUT = "logout";
    public static final String ACTION_TOGGLE_MIC = "microphone";
    public static final String ACTION_VIEW = "view";
    
    public static MeetingsActivity instance = null;
    
    
    MeetingsFragment f;
    
    public String url;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        instance = this;
        
        Intent i = getIntent();
        url = i.getStringExtra("url");
        if (url == null) {
            finish();
        }
    
        ActivityMeetingsBinding b = ActivityMeetingsBinding.inflate(getLayoutInflater());
        f = (MeetingsFragment) getSupportFragmentManager().findFragmentByTag("meetings_fragment");
        
        
        View decorView = getWindow().getDecorView();
        decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
                View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
    
        setContentView(b.getRoot());
    }
    
    
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String action = intent.getAction();
        //System.out.println("action: "+action);
        if (action == null) {
            finish();
            startActivity(intent);
        }
        // doing this raises a SecurityException on Android 12, so check the version
        if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.R) {
            sendBroadcast(new Intent(Intent.ACTION_CLOSE_SYSTEM_DIALOGS));
        }
    }
    
    @Override
    protected void onUserLeaveHint() {
        super.onUserLeaveHint();
    
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && getPackageManager().hasSystemFeature(PackageManager.FEATURE_PICTURE_IN_PICTURE)) {
            f.goFullscreen();
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                PictureInPictureParams.Builder b = new PictureInPictureParams.Builder();
                
                // https://stackoverflow.com/questions/54976761/how-do-i-create-a-custom-screen-size-for-picture-in-picture-mode-for-android
                
                
                
                // TODO
                //  for android S: b.setSeamlessResizeEnabled(true);
                //  f-droid doesn't have the android S sdk, so wait until android S is released
                
                LinkedList<RemoteAction> actions = new LinkedList<>();
                {
                    Intent i = new Intent(this, MeetingsReceiver.class);
                    i.setAction(ACTION_TOGGLE_MIC);
                    actions.add(new RemoteAction(
                            Icon.createWithResource(this, android.R.drawable.ic_btn_speak_now), getString(R.string.toggle_mic),
                            getString(R.string.toggle_mic), PendingIntent.getBroadcast(this, 0, i, PendingIntent.FLAG_IMMUTABLE)));
                }
                
                b.setActions(actions);
                
                
                enterPictureInPictureMode(b.build());
            } else {
                enterPictureInPictureMode();
            }
        }
    
    
    }
    
    @Override
    public void onPictureInPictureModeChanged(boolean isInPictureInPictureMode) {
        super.onPictureInPictureModeChanged(isInPictureInPictureMode);
        if (isInPictureInPictureMode) {
            
            
            
        } else {
            
            
            
        }
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (isFinishing()) {
            instance = null;
        }
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT < 24 || ! isInPictureInPictureMode()) {
            View decorView = getWindow().getDecorView();
            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    }
}
