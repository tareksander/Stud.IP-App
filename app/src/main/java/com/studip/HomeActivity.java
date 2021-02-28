package com.studip;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.widget.ActionMenuView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.studip.api.API;
import com.studip.api.User;

import java.lang.reflect.Modifier;

public class HomeActivity extends AppCompatActivity
{



    ViewPager2 pager;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        Data.home_activity = this;
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
        if (Data.api == null  || (! Data.api.logged_in()))
        {
            Intent intent = new Intent(this,MainActivity.class);
            startActivity(intent);
            finish();
        }
        if (Data.gson == null)
        {
            Data.gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();
            // include transient fields, we just don't want java serialization to try to serialize them
        }
        



        Data.user_provider = new User(HandlerCompat.createAsync(Looper.getMainLooper()));
        setContentView(R.layout.activity_home);
        
        
        ActionMenuView menu = findViewById(R.id.actionmenu);
        menu.setOnMenuItemClickListener(this::onOptionsItemSelected);
        
        getMenuInflater().inflate(R.menu.navigation_menu,menu.getMenu());
        
         

        Handler h = HandlerCompat.createAsync(Looper.getMainLooper());
        new Thread(new Runnable()
        {
            @Override
            public void run()
            {
                while (! Thread.interrupted())
                {
                    if (Data.data_restored)
                    {
                        h.post(new Runnable()
                        {
                            @Override
                            public void run()
                            {
                                if (Data.user == null)
                                {
                                    Data.user = Data.user_provider.getData();
                                }
                                pager.getAdapter().notifyDataSetChanged();
                            }
                        });
                    }
                    
                    try
                    {
                        Thread.sleep(10);
                    }
                    catch (InterruptedException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
        
        
        
        
        pager = findViewById(R.id.pager);
        FragmentStateAdapter pagerAdapter = new ScreenSlidePagerAdapter(this);
        pager.setAdapter(pagerAdapter);
    }
    


    @Override
    protected void onStop()
    {
        super.onStop();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        getMenuInflater().inflate(R.menu.navigation_menu, menu);
        return true;
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (! Data.data_restored)
        {
            return true;
        }
        switch (item.getItemId()) {
            case R.id.menu_home:
                pager.setCurrentItem(0);
                return true;
            case R.id.menu_events:
                pager.setCurrentItem(1);
                return true;
            case R.id.menu_files:
                pager.setCurrentItem(2);
                return true;
            case R.id.menu_messages:
                pager.setCurrentItem(3);
                return true;
            case R.id.menu_app_settings:
                Intent intent = new Intent(this,SettingsActivity.class);
                startActivity(intent);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    private class ScreenSlidePagerAdapter extends FragmentStateAdapter
    {

        public ScreenSlidePagerAdapter(@NonNull FragmentActivity fragmentActivity)
        {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position)
        {
            switch (position)
            {
                case 0:
                    return new HomeFragment();
                case 1:
                    return new CoursesFragment();
                case 2:
                    return new FileFragment();
                case 3:
                    return new MessagesFragment();
            }
            return null;
        }
        
        
        @Override
        public int getItemCount()
        {
            if (! Data.data_restored)
            {
                return 0;
            }
            return 4;
        }
    }








}