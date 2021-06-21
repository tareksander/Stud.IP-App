package org.studip.unofficial_app.ui;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.LabeledIntent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;

import java.util.LinkedList;

public class DeepLinkActivity extends AppCompatActivity
{
    // This is basically a redirect to HomeActivity, but it has to be like that,
    // because if a link is invalid and the activity returns instantly, there would be a small flash visible.
    // This activity is completely invisible, so there isn't a flash.
    // If the link is considered valid, it is redirected to HomeActivity.
    @Override
    protected void onNewIntent(Intent in)
    {
        super.onNewIntent(in);
        //System.out.println("newIntent");
        setIntent(in);
        handleIntent();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //System.out.println("created");
        Intent in = getIntent();
        if (in != null)
        {
            handleIntent();
        } else {
            finishAndRemoveTask();
        }
    }
    
    private void handleIntent() {
        Intent in = getIntent();
        if (in != null)
        {
            String action = in.getAction();
            Uri data = in.getData();
            if (Intent.ACTION_VIEW.equals(action) && data == null)
            {
                finishAndRemoveTask();
                return;
            }
            if (Intent.ACTION_SEND.equals(action) || Intent.ACTION_SEND_MULTIPLE.equals(action)) {
                Intent i = new Intent(this, ShareActivity.class);
                i.putExtra(Intent.EXTRA_INTENT, in);
                startActivity(i);
                finish();
                return;
            }
            if (! Intent.ACTION_VIEW.equals(in.getAction())) {
                finishAndRemoveTask();
                return;
            }
            // TODO mode the notification cancelling to the meetings receiver, for security
            long notification = in.getLongExtra(getApplicationContext().getPackageName()+".notification_id",-1);
            //System.out.println(notification);
            if (notification != -1) {
                NotificationManagerCompat m = NotificationManagerCompat.from(this);
                m.cancel((int) (notification % Integer.MAX_VALUE));
            }
            API api = APIProvider.getAPI(this);
            if (api == null)
            {
                toBrowser(in);
                // Toast.makeText(this, R.string.not_logged_in, Toast.LENGTH_SHORT).show();
            } else {
                if (api.getHostname().equals(data.getHost()))
                {
                    //System.out.println(data);
                    Intent i = new Intent(this, HomeActivity.class);
                    i.setAction(getApplicationContext().getPackageName()+".deeplink");
                    //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_DOCUMENT | Intent.FLAG_ACTIVITY_MULTIPLE_TASK | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
                    i.setData(data);
                    startActivity(i);
                } else {
                    toBrowser(in);
                }
            }
        }
        finish();
    }
    
    private void toBrowser(Intent in) {
        Settings s = SettingsProvider.getSettings(this);
        Intent i = new Intent();
        i.fillIn(in, 0);
        System.out.println(i.toUri(0));
        if (s.browser == null) {
            i = Intent.createChooser(i, getString(R.string.open_with));
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                LinkedList<LabeledIntent> activities = new LinkedList<>();
                PackageManager pm = getPackageManager();
                Intent webIntent = new Intent(Intent.ACTION_VIEW);
                webIntent.addCategory(Intent.CATEGORY_BROWSABLE);
                int flags = PackageManager.MATCH_DEFAULT_ONLY | PackageManager.MATCH_ALL;
                webIntent.setData(Uri.parse("https://test.domain.com.co.uk.tld")); // ensure the domain doesn't point to an app with app links
                for (ResolveInfo info : pm.queryIntentActivities(webIntent, flags)) {
                    if (info.activityInfo.applicationInfo.packageName.equals(getPackageName())) continue;
                    Intent activity = new Intent();
                    activity.fillIn(in, 0);
                    activity.setComponent(new ComponentName(info.activityInfo.packageName, info.activityInfo.name));
                    activities.add(new LabeledIntent(activity, info.activityInfo.applicationInfo.packageName,
                            info.activityInfo.applicationInfo.labelRes, info.activityInfo.applicationInfo.icon));
                }
                i = Intent.createChooser(new Intent(), getString(R.string.open_with));
                i.putExtra(Intent.EXTRA_INITIAL_INTENTS, activities.toArray(new LabeledIntent[0]));
            }
        } else {
            i.setComponent(ComponentName.unflattenFromString(s.browser));
        }
        try {
            startActivity(i);
        } catch (ActivityNotFoundException e) {
            //System.out.println("no activity found");
            if (! Intent.ACTION_CHOOSER.equals(i.getAction())) {
                i = Intent.createChooser(i, getString(R.string.open_with));
                try {
                    startActivity(i);
                } catch (ActivityNotFoundException ignored) {}
            }
        }
    }
    
    
}