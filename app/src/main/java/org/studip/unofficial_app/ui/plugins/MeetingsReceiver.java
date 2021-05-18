package org.studip.unofficial_app.ui.plugins;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

public class MeetingsReceiver extends BroadcastReceiver
{
    
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        //System.out.println(action);
        MeetingsActivity a = MeetingsActivity.instance;
        if (a != null) {
            if (MeetingsActivity.ACTION_LOGOUT.equals(action)) {
                a.finish();
            }
            if (a.f != null) {
                if (MeetingsActivity.ACTION_TOGGLE_MIC.equals(action)) {
                    a.f.toggleMic();
                }
            }
        }
    }
}
