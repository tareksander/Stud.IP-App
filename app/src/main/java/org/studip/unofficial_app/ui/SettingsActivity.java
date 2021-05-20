package org.studip.unofficial_app.ui;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.work.WorkManager;

import org.jetbrains.annotations.NotNull;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.databinding.ActivitySettingsBinding;
import org.studip.unofficial_app.documentsprovider.DocumentRoot;
import org.studip.unofficial_app.documentsprovider.DocumentsDB;
import org.studip.unofficial_app.documentsprovider.DocumentsDBProvider;
import org.studip.unofficial_app.documentsprovider.DocumentsProvider;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.NotificationWorker;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingsActivity extends AppCompatActivity implements AdapterView.OnItemSelectedListener
{
    public static int ProgressToPriority(int progress) {
        switch (progress) {
            case 0:
                return NotificationCompat.PRIORITY_MIN;
            case 1:
                return NotificationCompat.PRIORITY_LOW;
            case 2:
            default:
                return NotificationCompat.PRIORITY_DEFAULT;
            case 3:
                return NotificationCompat.PRIORITY_HIGH;
        }
    }
    
    public static int PriorityToProgress(int priority) {
        switch (priority) {
            case NotificationCompat.PRIORITY_MIN:
                return 0;
            case NotificationCompat.PRIORITY_LOW:
                return 1;
            case NotificationCompat.PRIORITY_DEFAULT:
            default:
                return 2;
            case NotificationCompat.PRIORITY_HIGH:
                return 3;
        }
    }
    
    private SharedPreferences sharedPreferences;
    private Settings settings;
    private ActivitySettingsBinding binding;
    private void setDialog(final RadioButton r, final int res) {
        r.setOnLongClickListener(v2 -> {
            new AlertDialog.Builder(this).setTitle(r.getText()).setMessage(getResources().getText(res)).show();
            return true; });
    }
    private final String NOTIFICATION_LAYOUT = "notification_layout_";
    private String[] notification_layout_titles;
    private String[] notification_layout_courses;
    private boolean[][] notification_layout_values;
    private String[][] notification_names;
    private String[][] notification_values;
    
    
    @Override
    protected void onSaveInstanceState(@NonNull @NotNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putStringArray(NOTIFICATION_LAYOUT+"titles", notification_layout_titles);
        outState.putStringArray(NOTIFICATION_LAYOUT+"courses", notification_layout_courses);
        outState.putSerializable(NOTIFICATION_LAYOUT+"values", notification_layout_values);
        outState.putSerializable(NOTIFICATION_LAYOUT+"names", notification_names);
        outState.putSerializable(NOTIFICATION_LAYOUT+"values2", notification_values);
    }
    
    @SuppressLint("CheckResult")
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
        
        
        switch (settings.notification_visibility) {
            case NotificationCompat.VISIBILITY_PUBLIC:
                binding.notificationVisibilityGroup.check(R.id.notification_public);
                break;
            case NotificationCompat.VISIBILITY_PRIVATE:
                binding.notificationVisibilityGroup.check(R.id.notification_private);
                break;
            default:
            case NotificationCompat.VISIBILITY_SECRET:
                binding.notificationVisibilityGroup.check(R.id.notification_secret);
        }
        
        
        if (Build.VERSION.SDK_INT >= 26) {
            binding.settingsNotificationLayout.setVisibility(View.GONE);
            binding.settingsAppSettings.setVisibility(View.GONE);
            binding.settingsNotificationChannels.setOnClickListener(vq -> {
                Intent i = new Intent(android.provider.Settings.ACTION_APP_NOTIFICATION_SETTINGS);
                // maybe
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra(android.provider.Settings.EXTRA_APP_PACKAGE,getPackageName());
                startActivity(i);
            });
            
        } else {
            binding.settingsNotificationLayout.setVisibility(View.VISIBLE);
            binding.settingsNotificationChannels.setVisibility(View.GONE);
            binding.settingsAppSettings.setOnClickListener(v1 -> {
                Intent i = new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                // maybe
                //i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.setData(Uri.parse("package:"+getPackageName()));
                startActivity(i);
            });
            
            binding.priorityForum.setProgress(PriorityToProgress(settings.forum_priority));
            binding.priorityFiles.setProgress(PriorityToProgress(settings.files_priority));
            binding.priorityMessages.setProgress(PriorityToProgress(settings.messages_priority));
            binding.priorityOther.setProgress(PriorityToProgress(settings.other_priority));
    
            binding.priorityForum.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    settings.forum_priority = ProgressToPriority(progress);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            binding.priorityFiles.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    settings.files_priority = ProgressToPriority(progress);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            binding.priorityMessages.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    System.out.println(progress);
                    settings.messages_priority = ProgressToPriority(progress);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            binding.priorityOther.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener()
            {
                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    settings.other_priority = ProgressToPriority(progress);
                }
                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {}
                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {}
            });
            
        }
        
        binding.notificationServiceEnabled.setOnClickListener(this::onNotificationServiceClicked);
        
        s.setOnItemSelectedListener(this);
        
        
        binding.enableDocumentsProvider.setChecked(settings.documents_provider);
        binding.enableDocumentsProvider.setOnClickListener(v1 -> {
                settings.documents_provider = binding.enableDocumentsProvider.isChecked();
                getContentResolver().notifyChange(DocumentsContract.buildRootsUri(DocumentsProvider.AUTHORITIES), null);
        });
    
        binding.enableDocThumbnails.setChecked(settings.documents_thumbnails);
        binding.enableDocSearch.setChecked(settings.documents_search);
        binding.enableDocRecents.setChecked(settings.documents_recents);
        
        binding.enableDocThumbnails.setOnClickListener(v1 -> {
            settings.documents_thumbnails = binding.enableDocThumbnails.isChecked();
            getContentResolver().notifyChange(DocumentsContract.buildRootsUri(DocumentsProvider.AUTHORITIES), null);
        });
        binding.enableDocSearch.setOnClickListener(v1 -> {
            settings.documents_search = binding.enableDocSearch.isChecked();
            getContentResolver().notifyChange(DocumentsContract.buildRootsUri(DocumentsProvider.AUTHORITIES), null);
        });
        binding.enableDocRecents.setOnClickListener(v1 -> {
            settings.documents_recents = binding.enableDocRecents.isChecked();
            getContentResolver().notifyChange(DocumentsContract.buildRootsUri(DocumentsProvider.AUTHORITIES), null);
        });
        
        
        DocumentsDB docs = DocumentsDBProvider.getDB(this);
    
        Transformations.distinctUntilChanged(docs.documents().observeRoots()).observe(this, roots -> {
            binding.documentsProviderCourses.removeAllViews();
            Arrays.sort(roots);
            for (DocumentRoot r : roots) {
                if (r.user) {
                    continue;
                }
                SwitchCompat c = new SwitchCompat(this);
                c.setChecked(r.enabled);
                c.setText(r.title);
                c.setOnClickListener(v1 -> {
                    r.enabled = c.isChecked();
                    docs.documents().updateInsertAsync(r).subscribeOn(Schedulers.io()).subscribe();
                });
                LinearLayout.LayoutParams p = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                p.bottomMargin = 8;
                binding.documentsProviderCourses.addView(c, p);
            }
        });
        
        
        
        
        binding.loadImagesOnMobile.setChecked(settings.load_images_on_mobile);
        
        // register the listener now that the checked status is set
        binding.loadImagesOnMobile.setOnClickListener(this::onLoadImagesOnMobileClicked);
        
        
        API api = APIProvider.getAPI(this);
        if (api == null || api.getUserID() == null) {
            binding.settingsLoadNotificationSettings.setVisibility(View.GONE);
            binding.settingsSaveNotificationSettings.setVisibility(View.GONE);
        } else {
            if (savedInstanceState != null) {
                /*
                outState.putStringArray(NOTIFICATION_LAYOUT+"titles", notification_layout_titles);
                outState.putStringArray(NOTIFICATION_LAYOUT+"courses", notification_layout_courses);
                outState.putSerializable(NOTIFICATION_LAYOUT+"values", notification_layout_values);
                 */
                notification_layout_titles = savedInstanceState.getStringArray(NOTIFICATION_LAYOUT+"titles");
                notification_layout_courses = savedInstanceState.getStringArray(NOTIFICATION_LAYOUT+"courses");
                try {
                    notification_layout_values = (boolean[][]) savedInstanceState.getSerializable(NOTIFICATION_LAYOUT + "values");
                    notification_names = (String[][]) savedInstanceState.getSerializable(NOTIFICATION_LAYOUT + "names");
                    notification_values = (String[][]) savedInstanceState.getSerializable(NOTIFICATION_LAYOUT + "values2");
                } catch (Exception ignored) {}
                buildNotificationSettings();
            }
        }
        
        
    }
    
    public void onLoadNotificationSettings(View v1) {
        API api = APIProvider.getAPI(this);
        if (api != null && api.getUserID() != null) {
            final MutableLiveData<Integer> remaining = new MutableLiveData<>(-1);
            api.dispatch.getNotificationSettings().enqueue(new Callback<String>()
            {
                @Override
                public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                    String html = response.body();
                    if (response.code() == 200 && html != null) {
                        Document d = Jsoup.parse(html);
                        Elements forms = d.getElementsByAttributeValueContaining("action",
                                "/dispatch.php/settings/notification/store");
                        if (forms.size() != 1) {
                            remaining.setValue(-2);
                            System.err.println("could not find NotificationTable");
                            return;
                        }
                        Element form = forms.get(0);
                        Elements closed = form.getElementsByAttributeValue("href", "/dispatch.php/settings/notification/open/");
                        remaining.setValue(closed.size());
                        for (Element c : closed) {
                            api.dispatch.openNotificationCategory(c.attr("name")).enqueue(new Callback<Void>()
                            {
                                @Override
                                public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                                    Integer i = remaining.getValue();
                                    i = (i == null) ? 1 : i;
                                    remaining.setValue(i-1);
                                }
                                @Override
                                public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                                    Integer i = remaining.getValue();
                                    i = (i == null) ? 1 : i;
                                    remaining.setValue(i-1);
                                }
                            });
                        }
                    }
                }
                @Override
                public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                    remaining.setValue(-2);
                }
            });
            remaining.observe(this, (v) -> {
                if (v == -1) return;
                if (v == -2) {
                    remaining.removeObservers(this);
                    return;
                }
                if (v == 0) {
                    api.dispatch.getNotificationSettings().enqueue(new Callback<String>()
                    {
                        @Override
                        public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                            String html = response.body();
                            if (response.code() == 200 && html != null) {
                                Document d = Jsoup.parse(html);
                                Elements forms = d.getElementsByAttributeValueContaining("action",
                                        "/dispatch.php/settings/notification/store");
                                if (forms.size() != 1) {
                                    remaining.setValue(-2);
                                    System.err.println("could not find NotificationTable");
                                    return;
                                }
                                Element form = forms.get(0);
                                Elements heads = form.getElementsByTag("thead");
                                if (heads.size() != 1) {
                                    remaining.setValue(-2);
                                    System.err.println("could not find NotificationTable head");
                                    return;
                                }
                                Element head = heads.get(0);
                                Elements categories = head.getElementsByTag("img");
                                int index = 0;
                                notification_layout_titles = new String[categories.size()];
                                for (Element c : categories) {
                                    notification_layout_titles[index] = c.attr("title");
                                    index++;
                                }
                                int width = categories.size();
                                Elements courses = form.getElementsByAttributeValueContaining("href", "/seminar_main.php?username=");
                                notification_layout_courses = new String[courses.size()];
                                notification_layout_values = new boolean[categories.size()][courses.size()];
                                notification_names = new String[categories.size()][courses.size()];
                                notification_values = new String[categories.size()][courses.size()];
                                index = 0;
                                for (Element c : courses) {
                                    notification_layout_courses[index] = c.text();
                                    Elements checkboxes = c.parent().parent().getElementsByAttributeValue("type", "checkbox");
                                    for (int x = 0; x < width; x++) {
                                        if (x < checkboxes.size()) {
                                            notification_layout_values[x][index] = checkboxes.get(x).hasAttr("checked");
                                        }
                                        notification_names[x][index] = checkboxes.get(x).attr("name");
                                        notification_values[x][index] = checkboxes.get(x).attr("value");
                                    }
                                    index++;
                                }
                                buildNotificationSettings();
                            }
                        }
                        @Override
                        public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {}
                    });
                }
            });
        }
    }
    
    public void buildNotificationSettings() {
        if (notification_layout_values == null ||
            notification_layout_courses == null ||
            notification_layout_titles == null ||
            notification_names == null) {
            return;
        }
        binding.studipNotificationSettings.removeAllViews();
        try {
            {
                TextView t = new TextView(this);
                t.setText(getString(R.string.courses));
                GridLayout.LayoutParams par = new GridLayout.LayoutParams();
                par.columnSpec = GridLayout.spec(0);
                par.rightMargin = 100;
                par.bottomMargin = 80;
                par.rowSpec = GridLayout.spec(0);
                binding.studipNotificationSettings.addView(t, par);
            }
            int index = 1;
            for (String title : notification_layout_titles) {
                TextView t = new TextView(this);
                t.setText(title);
                GridLayout.LayoutParams par = new GridLayout.LayoutParams();
                par.columnSpec = GridLayout.spec(index);
                if (index < notification_layout_titles.length) {
                    par.rightMargin = 50;
                }
                index++;
                par.bottomMargin = 80;
                par.rowSpec = GridLayout.spec(0);
                binding.studipNotificationSettings.addView(t, par);
            }
            index = 1;
            for (String text : notification_layout_courses) {
                TextView t = new TextView(this);
                t.setText(text);
                GridLayout.LayoutParams par = new GridLayout.LayoutParams();
                par.rowSpec = GridLayout.spec(index);
                if (index < notification_layout_courses.length) {
                    par.rightMargin = 100;
                }
                par.bottomMargin = 50;
                par.columnSpec = GridLayout.spec(0);
                binding.studipNotificationSettings.addView(t, par);
                index++;
            }
            for (int x = 0; x < notification_layout_values.length; x++) {
                for (int y = 0; y < notification_layout_values[0].length; y++) {
                    SwitchCompat s = new SwitchCompat(this);
                    int finalX = x;
                    int finalY = y;
                    s.setOnClickListener(v1 -> {
                        try {
                            notification_layout_values[finalX][finalY] = s.isChecked();
                        } catch (NullPointerException | ArrayIndexOutOfBoundsException ignored) {}
                    });
                    GridLayout.LayoutParams par2 = new GridLayout.LayoutParams();
                    if (notification_layout_values[x][y]) {
                        s.setChecked(true);
                    }
                    par2.columnSpec = GridLayout.spec(x + 1);
                    par2.bottomMargin = 50;
                    par2.rightMargin = 50;
                    par2.rowSpec = GridLayout.spec(y + 1);
                    binding.studipNotificationSettings.addView(s, par2);
                }
            }
        } catch (NullPointerException | ArrayIndexOutOfBoundsException ignored) {}
    }
    
    public void onSaveNotificationSettings(View v) {
        API api = APIProvider.getAPI(this);
        final AppCompatActivity a = this;
        if (api != null && api.getUserID() != null && notification_layout_values != null
                && notification_names != null && notification_values != null) {
            api.dispatch.getNotificationSettings().enqueue(new Callback<String>()
            {
                @Override
                public void onResponse(@NotNull Call<String> call, @NotNull Response<String> response) {
                    String html = response.body();
                    if (response.code() == 200 && html != null) {
                        Document d = Jsoup.parse(html);
                        Elements forms = d.getElementsByAttributeValueContaining("action",
                                "/dispatch.php/settings/notification/store");
                        if (forms.size() != 1) {
                            System.err.println("could not find NotificationTable");
                        }
                        Element form = forms.get(0);
                        Elements ticket = form.getElementsByAttributeValue("name", "studip_ticket");
                        Elements token = form.getElementsByAttributeValue("name", "security_token");
                        if (ticket.size() != 1 || token.size() != 1) {
                            System.err.println("could not find security_token or studip_ticket");
                            Toast.makeText(a, R.string.notification_settings_error, Toast.LENGTH_LONG).show();
                            return;
                        }
                        HashMap<String, String> fields = new HashMap<>();
                        fields.put("studip_ticket", ticket.get(0).attr("value"));
                        fields.put("security_token", token.get(0).attr("value"));
                        fields.put("store", "");
                        for (int x = 0;x < notification_layout_values.length; x++) {
                            for (int y = 0; y < notification_layout_values[0].length; y++) {
                                if (notification_layout_values[x][y]) {
                                    fields.put(notification_names[x][y], notification_values[x][y]);
                                }
                            }
                        }
                        api.dispatch.saveNotificationSettings(fields).enqueue(new Callback<Void>()
                        {
                            @Override
                            public void onResponse(@NotNull Call<Void> call, @NotNull Response<Void> response) {
                                if (response.code() != 302 && response.code() != 200) {
                                    Toast.makeText(a, R.string.notification_settings_error, Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(a, R.string.notification_settings_success, Toast.LENGTH_SHORT).show();
                                }
                            }
                            @Override
                            public void onFailure(@NotNull Call<Void> call, @NotNull Throwable t) {
                                Toast.makeText(a, R.string.notification_settings_error, Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                }
                @Override
                public void onFailure(@NotNull Call<String> call, @NotNull Throwable t) {
                    Toast.makeText(a, R.string.notification_settings_error, Toast.LENGTH_LONG).show();
                }
            });
        }
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        settings.safe(sharedPreferences);
    }
    
    public void onLoadImagesOnMobileClicked(View v) {
        settings.load_images_on_mobile = binding.loadImagesOnMobile.isChecked();
        getContentResolver().notifyChange(DocumentsContract.buildRootsUri(DocumentsProvider.AUTHORITIES), null);
    }
    
    
    public void onHelpClicked(View v) {
        Intent i = new Intent(this,HelpActivity.class);
        startActivity(i);
    }
    
    public void onVisibilityClicked(View v) {
        if (v.equals(binding.notificationPublic)) {
            settings.notification_visibility = NotificationCompat.VISIBILITY_PUBLIC;
        }
        if (v.equals(binding.notificationPrivate)) {
            settings.notification_visibility = NotificationCompat.VISIBILITY_PRIVATE;
        }
        if (v.equals(binding.notificationSecret)) {
            settings.notification_visibility = NotificationCompat.VISIBILITY_SECRET;
        }
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
    
        NotificationManagerCompat m = NotificationManagerCompat.from(this);
        m.cancelAll();
        
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
        if (((Integer) s.getAdapter().getItem(position)) != settings.notification_period) {
            settings.notification_period = (Integer) s.getAdapter().getItem(position);
            NotificationWorker.enqueue(this);
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent)
    {

    }
}