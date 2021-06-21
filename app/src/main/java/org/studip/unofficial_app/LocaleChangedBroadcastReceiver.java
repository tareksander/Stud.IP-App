package org.studip.unofficial_app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import org.studip.unofficial_app.model.Notifications;

public class LocaleChangedBroadcastReceiver extends BroadcastReceiver
{
    @Override
    public void onReceive(Context context, Intent intent) {
        //System.out.println("locale changed");
        Notifications.initChannels(context);
        for (ShortcutInfoCompat info : ShortcutManagerCompat.getDynamicShortcuts(context)) {
            //System.out.println(info.getShortLabel());
        }
    }
}
