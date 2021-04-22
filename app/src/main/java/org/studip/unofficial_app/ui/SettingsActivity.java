package org.studip.unofficial_app.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.work.WorkManager;


import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.databinding.ActivitySettingsBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.NotificationWorker;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;

import java.util.ArrayList;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    private SharedPreferences sharedPreferences;
    private Settings settings;
    private ActivitySettingsBinding binding;
    private void setDialog(final RadioButton r, final int res) {
        r.setOnLongClickListener(v2 -> {
            new AlertDialog.Builder(this).setTitle(r.getText()).setMessage(getResources().getText(res)).show();
            return true; });
    }
    
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        sharedPreferences = SettingsProvider.getSettingsPreferences(this);
        if (sharedPreferences == null) {
            Intent intent = new Intent(this, ServerSelectActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        settings = SettingsProvider.getSettings(this);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        
        setContentView(binding.getRoot());
        RadioGroup auth_group = binding.authGroup;
        switch (settings.authentication_method)
        {
            default:
            case Settings.AUTHENTICATION_BASIC:
                auth_group.check(R.id.auth_basic);
                break;
            case Settings.AUTHENTICATION_COOKIE:
                auth_group.check(R.id.auth_cookie);
                break;
            case Settings.AUTHENTICATION_OAUTH:
                auth_group.check(R.id.auth_oauth);
                break;
        }
        
        
        setDialog(binding.authBasic,R.string.auth_basic_desc);
        setDialog(binding.authCookie,R.string.auth_cookie_desc);
        setDialog(binding.authOauth,R.string.auth_oauth_desc);
        
        
        RadioGroup theme_group = binding.themeGroup;
        switch (settings.theme)
        {
            default:
            case AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM:
                theme_group.check(R.id.theme_auto);
                break;
            case AppCompatDelegate.MODE_NIGHT_NO:
                theme_group.check(R.id.theme_light);
                break;
            case AppCompatDelegate.MODE_NIGHT_YES:
                theme_group.check(R.id.theme_dark);
                break;
        }
        SwitchCompat sw = binding.notificationServiceEnabled;
        sw.setChecked(settings.notification_service_enabled);
        Spinner s = binding.notificationServicePeriod;

        List<Integer> list = new ArrayList<>();
        for (int i : getResources().getIntArray(R.array.service_periods))
        {
            Integer integer = i;
            list.add(integer);
        }
        ArrayAdapter<Integer> a = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, list.toArray(new Integer[0]));
        s.setAdapter(a);
        s.setSelection(a.getPosition(settings.notification_period));
        if (! settings.notification_service_enabled)
        {
            s.setEnabled(false);
        }
        s.setOnItemSelectedListener(this);
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        settings.safe(sharedPreferences);
    }

    public void onThemeClicked(View v)
    {
        if (v.equals(binding.themeAuto))
        {
            settings.theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        if (v.equals(binding.themeDark))
        {
            settings.theme = AppCompatDelegate.MODE_NIGHT_YES;
        }
        if (v.equals(binding.themeLight))
        {
            settings.theme = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(settings.theme);
    }

    public void onNotificationServiceClicked(View v)
    {
        SwitchCompat s = (SwitchCompat) v;
        settings.notification_service_enabled = s.isChecked();
        Spinner sp = binding.notificationServicePeriod;
        sp.setEnabled(settings.notification_service_enabled);
        if (settings.notification_service_enabled) {
            NotificationWorker.enqueue(this);
        } else {
            WorkManager.getInstance(this).cancelUniqueWork(NotificationWorker.WORKER_ID);
        }
    }
    
    public void onAuthMethodClicked(View v)
    {
        if (v.equals(binding.authBasic))
        {
            if (settings.authentication_method != Settings.AUTHENTICATION_BASIC) {
                changeAuthentication();
            }
            settings.authentication_method = Settings.AUTHENTICATION_BASIC;
            return;
        }
        if (v.equals(binding.authCookie))
        {
            if (settings.authentication_method != Settings.AUTHENTICATION_COOKIE) {
                changeAuthentication();
            }
            settings.authentication_method = Settings.AUTHENTICATION_COOKIE;
            return;
        }
        if (v.equals(binding.authOauth))
        {
            if (settings.authentication_method != Settings.AUTHENTICATION_OAUTH) {
                changeAuthentication();
            }
            settings.authentication_method = Settings.AUTHENTICATION_OAUTH;
        }
    }
    
    private void changeAuthentication() {
        API api = APIProvider.getAPI(this);
        EncryptedSharedPreferences prefs = APIProvider.getPrefs(this);
        if (api != null && prefs != null) {
            api.logout(prefs);
            APIProvider.loadAPI(this);
            Intent i = new Intent(this, LoginActivity.class);
            startActivity(i);
            finish();
        }
    }
    
    
    @SuppressLint("ApplySharedPref")
    void logout()
    {
        EncryptedSharedPreferences prefs = APIProvider.getPrefs(this);
        if (prefs != null) {
            prefs.edit().clear().commit();
        }
        
        WorkManager.getInstance(this).cancelUniqueWork(NotificationWorker.WORKER_ID);
        //System.out.println("logout");
        settings.logout = true;
        settings.safe(SettingsProvider.getSettingsPreferences(this));
        Intent i = new Intent(this,HomeActivity.class);
        startActivity(i);
        System.exit(0);
    }
    
    public void onDeleteCredentials(View v)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.clear_credentials);
        b.setMessage(R.string.clear_credentials_dialog);
        b.setPositiveButton(R.string.ok, (dialog, which) ->
        {
            logout();
            dialog.cancel();
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        b.show();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Spinner s = (Spinner) parent;
        settings.notification_period = (Integer) s.getAdapter().getItem(position);
        NotificationWorker.enqueue(this);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}