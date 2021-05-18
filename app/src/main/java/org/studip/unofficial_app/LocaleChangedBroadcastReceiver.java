package org.studip.unofficial_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import org.studip.unofficial_app.model.Notifications;

public class LocaleChangedBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        Notifications.initChannels(context);
    }
}
