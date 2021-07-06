package org.studip.unofficial_app.ui;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.LiveData;
import androidx.security.crypto.EncryptedSharedPreferences;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.OAuthUtils;
import org.studip.unofficial_app.databinding.ActivityLoginBinding;
import org.studip.unofficial_app.databinding.ActivityLoginOauthBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.ui.fragments.dialog.DiscoveryErrorDialogFragment;
import org.studip.unofficial_app.ui.fragments.dialog.LoginErrorDialogFragment;
import org.studip.unofficial_app.ui.fragments.dialog.OAuthDisabledDialogFragment;
import org.studip.unofficial_app.ui.fragments.dialog.TextDialogFragment;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


public class LoginActivity extends AppCompatActivity
{
    private ActivityLoginOauthBinding oauthb;
    private ActivityLoginBinding loginb;
    private boolean login_running = false;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        final API api = APIProvider.getAPI(this);
        if (api == null || api.getHostname() == null) {
            Intent intent = new Intent(this, ServerSelectActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        final AppCompatActivity a = this;
        
        Settings s = SettingsProvider.getSettings(this);
        if (s.authentication_method == Settings.AUTHENTICATION_OAUTH) {
            oauthb = ActivityLoginOauthBinding.inflate(getLayoutInflater());
    
            if (! OAuthUtils.hosts.containsKey(api.getHostname())) {
                new OAuthDisabledDialogFragment().show(getSupportFragmentManager(), "oauth_disabled");
                oauthb = null;
                setContentView(new ConstraintLayout(this));
            } else {
                oauthb.oauthExplain.setText(HelpActivity.fromHTML(getString(R.string.oauth_login_explain), false, null));
                
                oauthb.oauthRedirect.setOnClickListener(v1 -> {
                    Call<String> tok = OAuthUtils.requestToken(api);
                    if (tok == null) {
                        new OAuthDisabledDialogFragment().show(getSupportFragmentManager(), "oauth_disabled");
                    } else {
                        tok.enqueue(new Callback<String>()
                        {
                            @Override
                            public void onResponse(@NonNull Call<String> call, @NonNull Response<String> response) {
                                String body = response.body();
                                if (body != null) {
                                    OAuthUtils.OAuthToken temp = OAuthUtils.getTokenFromResponse(body, true);
                                    if (temp == null) {
                                        Bundle args = new Bundle();
                                        args.putString(TextDialogFragment.TITLE, getString(R.string.oauth_error_title));
                                        args.putString(TextDialogFragment.TEXT, getString(R.string.oauth_error_no_token));
                                        args.putString(TextDialogFragment.BUTTON_TEXT, getString(R.string.ok));
                                        TextDialogFragment t = new TextDialogFragment();
                                        t.setArguments(args);
                                        t.show(getSupportFragmentManager(), "oauth_error_dialog");
                                        return;
                                    }
                                    api.setToken(temp);
                                    api.authToken(a);
                                    //System.out.println(API.HTTPS+api.getHostname()+OAuthUtils.authorize_url+"?oauth_token="+temp.oauth_token);
                                }
                            }
    
                            @Override
                            public void onFailure(@NonNull Call<String> call, @NonNull Throwable th) {
                                Bundle args = new Bundle();
                                args.putString(TextDialogFragment.TITLE, getString(R.string.oauth_error_title));
                                args.putString(TextDialogFragment.TEXT, getString(R.string.oauth_error_no_token));
                                args.putString(TextDialogFragment.BUTTON_TEXT, getString(R.string.ok));
                                TextDialogFragment t = new TextDialogFragment();
                                t.setArguments(args);
                                t.show(getSupportFragmentManager(), "oauth_error_dialog");
                            }
                        });
                    }
                });
                oauthb.oauthContinue.setOnClickListener(v1 -> {
                    LiveData<Integer> d = api.login(this, null, null, Settings.AUTHENTICATION_OAUTH);
                    d.observe(this, code -> {
                        if (code == -1) {
                            return;
                        }
                        d.removeObservers(this);
                        //System.out.println(code);
                        if (code == 200) {
                            toHome();
                        } else {
                            Bundle args = new Bundle();
                            args.putString(TextDialogFragment.TITLE, getString(R.string.oauth_error_title));
                            args.putString(TextDialogFragment.TEXT, getString(R.string.oauth_error_token_invalid));
                            args.putString(TextDialogFragment.BUTTON_TEXT, getString(R.string.ok));
                            TextDialogFragment t = new TextDialogFragment();
                            t.setArguments(args);
                            t.show(getSupportFragmentManager(), "oauth_error_dialog");
                        }
                    });
                });
                setContentView(oauthb.getRoot());
            }
        } else {
            loginb = ActivityLoginBinding.inflate(getLayoutInflater());
            loginb.submitLogin.setOnClickListener(this::onLogin);
            setContentView(loginb.getRoot());
        }
        
        
        
        
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
        TextView username = loginb.username;
        TextView password = loginb.password;
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