package com.studip;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.appcompat.widget.TooltipCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Switch;

import com.studip.api.API;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        try
        {
            MasterKey.Builder b = new MasterKey.Builder(this);
            b.setKeyScheme(MasterKey.KeyScheme.AES256_GCM);
            MasterKey m = b.build();
            sharedPreferences = EncryptedSharedPreferences.create(
                    this,
                    "secret_shared_prefs",
                    m,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            if (Data.settings == null)
            {
                Data.settings = Settings.load(sharedPreferences);
                AppCompatDelegate.setDefaultNightMode(Data.settings.theme);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        setContentView(R.layout.activity_settings);
        RadioGroup auth_group = findViewById(R.id.auth_group);
        switch (Data.settings.authentication_method)
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
        // TODO manually display dialogs, as the tooltips cut off the text
        RadioButton auth = findViewById(R.id.auth_basic);
        TooltipCompat.setTooltipText(auth,getResources().getText(R.string.auth_basic_desc));
        auth = findViewById(R.id.auth_cookie);
        TooltipCompat.setTooltipText(auth,getResources().getText(R.string.auth_cookie_desc));
        auth = findViewById(R.id.auth_oauth);
        TooltipCompat.setTooltipText(auth,getResources().getText(R.string.auth_oauth_desc));
        
        
        
        
        RadioGroup theme_group = findViewById(R.id.theme_group);
        switch (Data.settings.theme)
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
        SwitchCompat sw = findViewById(R.id.notification_service_enabled);
        sw.setChecked(Data.settings.notification_service_enabled);
        Spinner s = findViewById(R.id.notification_service_period);

        List<Integer> list = new ArrayList<>();
        for (int i : getResources().getIntArray(R.array.service_periods))
        {
            Integer integer = i;
            list.add(integer);
        }
        ArrayAdapter<Integer> a = new ArrayAdapter<Integer>(this,android.R.layout.simple_spinner_item, list.toArray(new Integer[0]));
        s.setAdapter(a);
        s.setSelection(a.getPosition(Data.settings.notification_period));
        if (! Data.settings.notification_service_enabled)
        {
            s.setEnabled(false);
        }
        s.setOnItemSelectedListener(this);
    }


    @Override
    protected void onStop()
    {
        super.onStop();
        Data.settings.safe(sharedPreferences);
    }

    public void onThemeClicked(View v)
    {
        if (v.equals(findViewById(R.id.theme_auto)))
        {
            Data.settings.theme = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;
        }
        if (v.equals(findViewById(R.id.theme_dark)))
        {
            Data.settings.theme = AppCompatDelegate.MODE_NIGHT_YES;
        }
        if (v.equals(findViewById(R.id.theme_light)))
        {
            Data.settings.theme = AppCompatDelegate.MODE_NIGHT_NO;
        }
        AppCompatDelegate.setDefaultNightMode(Data.settings.theme);
    }

    public void onNotificationServiceClicked(View v)
    {
        SwitchCompat s = (SwitchCompat) v;
        Data.settings.notification_service_enabled = s.isChecked();
        Spinner sp = findViewById(R.id.notification_service_period);
        sp.setEnabled(Data.settings.notification_service_enabled);
    }
    
    public void onAuthMethodClicked(View v)
    {
        if (v.equals(findViewById(R.id.auth_basic)))
        {
            Data.settings.authentication_method = Settings.AUTHENTICATION_BASIC;
            return;
        }
        if (v.equals(findViewById(R.id.auth_cookie)))
        {
            Data.settings.authentication_method = Settings.AUTHENTICATION_COOKIE;
            return;
        }
        if (v.equals(findViewById(R.id.auth_oauth)))
        {
            Data.settings.authentication_method = Settings.AUTHENTICATION_OAUTH;
        }
    }
    
    void logout()
    {
        if (Data.api != null)
        {
            try
            {
                Data.api.logout(sharedPreferences);
            }
            catch (Exception ignored)
            {
            }
        }
        else
        {
            if (sharedPreferences instanceof EncryptedSharedPreferences)
            {
                SharedPreferences.Editor e = sharedPreferences.edit();
                e.remove(API.CREDENTIALS_SERVER);
                e.remove(API.CREDENTIALS_USERNAME);
                e.remove(API.CREDENTIALS_PASSWORD);
                e.apply();
            }
        }
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    public void onDeleteCredentials(View v)
    {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle(R.string.clear_credentials);
        b.setMessage(R.string.clear_credentials_dialog);
        b.setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                logout();
                dialog.cancel();
            }
        });
        b.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                dialog.cancel();
            }
        });
        b.show();
    }


    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id)
    {
        Spinner s = (Spinner) parent;
        Data.settings.notification_period = (Integer) s.getAdapter().getItem(position);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}