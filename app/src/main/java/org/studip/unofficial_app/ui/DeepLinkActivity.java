package org.studip.unofficial_app.ui;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationManagerCompat;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.model.APIProvider;

public class DeepLinkActivity extends AppCompatActivity
{
    // This is basically a redirect to HomeActivity, but it has to be like that, because if a link is invalid and the activity returns instantly, there would be a small flash visible.
    // This activity is completely invisible, so there isn't a flash.
    // If the link is considered valid, it is redirected to HomeActivity.
    @Override
    protected void onNewIntent(Intent in)
    {
        super.onNewIntent(in);
        if (in != null)
        {
            String action = in.getAction();
            Uri data = in.getData();
            if (! Intent.ACTION_VIEW.equals(action) || data == null)
            {
                finishAndRemoveTask();
                return;
            }
            long notification = in.getLongExtra(getApplicationContext().getPackageName()+".notification_id",-1);
            //System.out.println(notification);
            if (notification != -1) {
                NotificationManagerCompat m = NotificationManagerCompat.from(this);
                m.cancel((int) (notification % Integer.MAX_VALUE));
            }
            API api = APIProvider.getAPI(this);
            if (api == null)
            {
                Toast.makeText(this, R.string.not_logged_in, Toast.LENGTH_SHORT).show();
            } else {
                if (api.getHostname().equals(data.getHost()))
                {
                    Intent i = new Intent(this, HomeActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.setData(data);
                    startActivity(i);
                }
            }
        }
        finish();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Intent in = getIntent();
        if (in != null)
        {
            onNewIntent(getIntent());
        } else {
            finishAndRemoveTask();
        }
    }
    
}