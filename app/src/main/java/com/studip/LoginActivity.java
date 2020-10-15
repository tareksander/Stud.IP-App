package com.studip;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.studip.api.API;


public class LoginActivity extends AppCompatActivity
{

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        try
        {
            MasterKey.Builder b = new MasterKey.Builder(this);
            b.setKeyScheme(MasterKey.KeyScheme.AES256_GCM);
            MasterKey m = b.build();
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
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
            if (Data.api == null)
            {
                Data.api = API.restore(sharedPreferences,this);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        if (Data.api != null && Data.api.logged_in())
        {
            Intent intent = new Intent(this,HomeActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    
    @Override
    public void onStop()
    {
        super.onStop();
        try
        {
            MasterKey.Builder b = new MasterKey.Builder(this);
            b.setKeyScheme(MasterKey.KeyScheme.AES256_GCM);
            MasterKey m = b.build();
            SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                    this,
                    "secret_shared_prefs",
                    m,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            Data.api.save(sharedPreferences);
        }
        catch (Exception e)
        {
            e.printStackTrace();
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
    
    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
    
    public void onLogin(View v)
    {
        //new AlertDialog.Builder(this).setTitle(R.string.login_error_title).setMessage(R.string.login_error_message).show();
        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        try
        {
            if (Data.api.login(username.getText().toString(),password.getText().toString().toCharArray()))
            {
                try
                {
                    MasterKey.Builder b = new MasterKey.Builder(this);
                    b.setKeyScheme(MasterKey.KeyScheme.AES256_GCM);
                    MasterKey m = b.build();
                    SharedPreferences sharedPreferences = EncryptedSharedPreferences.create(
                            this,
                            "secret_shared_prefs",
                            m,
                            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
                    );
                    Data.api.save(sharedPreferences);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                    Intent intent = new Intent(this,MainActivity.class);
                    startActivity(intent);
                    finish();
                }
                Intent intent = new Intent(this,HomeActivity.class);
                startActivity(intent);
                finish();
                return;
            }
        }
        catch (Exception e) {}
        new AlertDialog.Builder(this).setTitle(R.string.login_error_title).setMessage(R.string.login_error_message).show();
    }
    
    
    
    
    
    
}