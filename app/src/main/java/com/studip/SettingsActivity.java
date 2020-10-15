package com.studip;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
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
import android.widget.RadioGroup;

import com.studip.api.API;

public class SettingsActivity extends AppCompatActivity
{

    SharedPreferences sharedPreferences;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
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
        RadioGroup auth_group = findViewById(R.id.auth_group);
        switch (Data.settings.authentification_method)
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
    
    public void onAuthMethodClicked(View v)
    {
        if (v.equals(findViewById(R.id.auth_basic)))
        {
            Data.settings.authentification_method = Settings.AUTHENTICATION_BASIC;
            return;
        }
        if (v.equals(findViewById(R.id.auth_cookie)))
        {
            Data.settings.authentification_method = Settings.AUTHENTICATION_COOKIE;
            return;
        }
        if (v.equals(findViewById(R.id.auth_oauth)))
        {
            Data.settings.authentification_method = Settings.AUTHENTICATION_OAUTH;
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
    
    
    
    
    
    
}