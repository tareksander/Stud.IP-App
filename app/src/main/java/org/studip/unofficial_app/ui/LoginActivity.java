package org.studip.unofficial_app.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LiveData;
import androidx.security.crypto.EncryptedSharedPreferences;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.ui.fragments.dialog.DiscoveryErrorDialogFragment;
import org.studip.unofficial_app.ui.fragments.dialog.LoginErrorDialogFragment;


public class LoginActivity extends AppCompatActivity
{
    private boolean login_running = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        API api = APIProvider.getAPI(this);
        if (api == null) {
            Intent intent = new Intent(this, ServerSelectActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        
        
        /*
        if (api != null && api.logged_in(this) == 200)
        {
            Intent intent = new Intent(this,HomeActivity.class);
            startActivity(intent);
            finish();
            return;
        }
         */
        
        
        
        setContentView(R.layout.activity_login);
    }
    
    
    @Override
    public void onStop()
    {
        super.onStop();
        API api = APIProvider.getAPI(this);
        if (api != null) {
            EncryptedSharedPreferences prefs = APIProvider.getPrefs(this);
            if (prefs != null)
            {
                api.save(prefs);
            }
        }
    }
    
    @Override
    public void onBackPressed()
    {
        finish();
    }
    
    private void toHome() {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }
    
    public void onLogin(View v)
    {
        TextView username = findViewById(R.id.username);
        TextView password = findViewById(R.id.password);
        if (! login_running)
        {
            login_running = true;
            final AppCompatActivity c = this;
            API api = APIProvider.getAPI(this);
            if (api == null) {
                return;
            }
            LiveData<Integer> l = api.login(this, username.getText().toString(), password.getText().toString(), SettingsProvider.getSettings(this).authentication_method);
            l.observe(this, code ->
            {
                if (code != -1)
                {
                    if (code == 200)
                    {
                        LiveData<Integer> f = api.discover();
                        f.observe(this, code2 -> {
                            if (code2 == -1) {
                                return;
                            }
                            EncryptedSharedPreferences p = APIProvider.getPrefs(c);
                            if (p != null) {
                                api.save(p);
                            }
                            login_running = false;
                            l.removeObservers(c);
                            DiscoveryErrorDialogFragment e = new DiscoveryErrorDialogFragment();
                            Bundle args = new Bundle();
                            args.putInt(DiscoveryErrorDialogFragment.CODE, code2);
                            e.setArguments(args);
                            e.show(getSupportFragmentManager(), "discovery_error");
                        });
                    }
                    else
                    {
                        l.removeObservers(c);
                        login_running = false;
                        LoginErrorDialogFragment f = new LoginErrorDialogFragment();
                        Bundle args = new Bundle();
                        args.putInt(LoginErrorDialogFragment.ERROR_CODE, code);
                        f.setArguments(args);
                        f.show(getSupportFragmentManager(), "login_error");
                    }
                }
            });
        }
    }
    
    
    
    
    
    
}