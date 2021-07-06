package org.studip.unofficial_app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.ComponentCallbacks2;
import android.content.ComponentName;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.Person;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.databinding.ActivityHomeBinding;
import org.studip.unofficial_app.documentsprovider.DocumentsDB;
import org.studip.unofficial_app.documentsprovider.DocumentsDBProvider;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.NotificationWorker;
import org.studip.unofficial_app.model.Notifications;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.model.viewmodels.CoursesViewModel;
import org.studip.unofficial_app.model.viewmodels.FileViewModel;
import org.studip.unofficial_app.model.viewmodels.HomeActivityViewModel;
import org.studip.unofficial_app.model.viewmodels.MessagesViewModel;
import org.studip.unofficial_app.ui.fragments.CoursesFragment;
import org.studip.unofficial_app.ui.fragments.FileFragment;
import org.studip.unofficial_app.ui.fragments.HomeFragment;
import org.studip.unofficial_app.ui.fragments.MessageFragment;
import org.studip.unofficial_app.ui.fragments.dialog.CourseForumDialogFragment;
import org.studip.unofficial_app.ui.plugins.MeetingsActivity;
import org.studip.unofficial_app.ui.plugins.fragments.dialog.CourseOpencastDialog;
import org.studip.unofficial_app.ui.plugins.fragments.dialog.CoursewareDialog;
import org.studip.unofficial_app.ui.plugins.fragments.dialog.MeetingsRoomsDialog;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeActivity extends AppCompatActivity implements ComponentCallbacks2
{
    public static final Pattern courseFilesPattern = Pattern.compile("/dispatch\\.php/course/files?(/index)\\?cid=(\\p{Alnum}+)$");
    public static final Pattern courseFilesPatternFolder = Pattern.compile("/dispatch\\.php/course/files/index/(\\p{Alnum}+)\\?cid=(\\p{Alnum}+)$");
    
    public static final Pattern userFilePattern = Pattern.compile("/dispatch\\.php/files/index/(\\p{Alnum}+)$");
    
    public static final Pattern fileDetailsPattern = Pattern.compile("/dispatch\\.php/file/details/(\\p{Alnum}+)(\\?cid=\\p{Alnum}+)??");
    
    
    
    
    public static final Pattern courseForumPattern = Pattern.compile("/plugins\\.php/coreforum/index\\?cid=(\\p{Alnum}+)$");
    public static final Pattern courseForumPattern2 = Pattern.compile("/plugins\\.php/coreforum/index/index\\?cid=(\\p{Alnum}+)$");
    public static final Pattern courseForumEntryPattern = Pattern.compile("/plugins\\.php/coreforum/index/(\\p{Alnum}+)\\?cid=(\\p{Alnum}+)$");
    public static final Pattern courseForumEntryPattern2 = Pattern.compile("/plugins\\.php/coreforum/index/index/(\\p{Alnum}+)\\?cid=(\\p{Alnum}+)$");
    
    public static final Pattern courseMembersPattern = Pattern.compile("/dispatch\\.php/course/members\\?cid=(\\p{Alnum}+)$");

    public static final Pattern courseCoursewarePattern = 
            Pattern.compile("/plugins\\.php/courseware/courseware\\?cid=(\\p{Alnum}+)(&selected=(\\p{Alnum}+))?");
    
    public static final Pattern messagePattern = Pattern.compile("/dispatch.php/messages/read/(\\p{Alnum}+)");
    public static final Pattern messagePattern2 = Pattern.compile("/dispatch.php/messages/overview/(\\p{Alnum}+)");
    
    public static final Pattern opencastPattern = Pattern.compile("/plugins.php/opencast/course/index/false\\?cid=(\\p{Alnum}+)");
    public static final Pattern meetingsPattern = Pattern.compile("/plugins.php/meetingplugin/index\\?cid=(\\p{Alnum}+)");
    public static final Pattern meetingsJoinPattern = Pattern.compile("/plugins.php/meetingplugin/api/rooms/join/(.+)");
    
    
    
    private ActivityHomeBinding binding;
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        

        Notifications.initChannels(this);

        Settings s = SettingsProvider.getSettings(this);
        AppCompatDelegate.setDefaultNightMode(SettingsProvider.getSettings(this).theme);
        if (s.logout) {
            s.logout = false;
            //System.out.println("clearing database");
            // set logout to false an safe immediately
            s.safe(SettingsProvider.getSettingsPreferences(this));
            
            // clear all databases
            DB db = DBProvider.getDB(this);
            db.getTransactionExecutor().execute(db::clearAllTables);
            DocumentsDB docdb = DocumentsDBProvider.getDB(this);
            docdb.getTransactionExecutor().execute(docdb::clearAllTables);
            // re-enable the notification worker
            if (s.notification_service_enabled) {
                NotificationWorker.enqueue(this);
            }
            // remove all dynamic shortcuts
            // disable them first, because pinned shortcuts can't be removed, but can only be disabled
            LinkedList<String> ids = new LinkedList<>();
            for (ShortcutInfoCompat info : ShortcutManagerCompat.getDynamicShortcuts(this)) {
                ids.add(info.getId());
            }
            ShortcutManagerCompat.disableShortcuts(this, ids, getString(R.string.shortcut_disabled));
            ShortcutManagerCompat.removeAllDynamicShortcuts(this);
            // return the user to the login screen
            Intent intent = new Intent(this,ServerSelectActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        
        API api = APIProvider.getAPI(this);
        if (api == null || api.getUserID() == null) {
            Intent i = new Intent(this, ServerSelectActivity.class);
            startActivity(i);
            finish();
            return;
        }
    
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        
        final Activity a = this;
        for (int i = 0;i<binding.tabs.getTabCount();i++) {
            final int finalI = i;
            TabLayout.Tab t = binding.tabs.getTabAt(i);
            if (t != null) {
                t.view.setOnLongClickListener(v -> {
                    System.out.println("long press on " + finalI);
                    String data = null;
                    String label = null;
                    int icon = 0;
                    switch (finalI) {
                        case 1:
                            data = "courses";
                            icon = R.drawable.seminar_blue;
                            label = getString(R.string.courses);
                            break;
                        case 2:
                            data = "files";
                            icon = R.drawable.file_blue;
                            label = getString(R.string.channel_files);
                            break;
                        case 3:
                            data = "messages";
                            icon = R.drawable.mail_blue;
                            label = getString(R.string.channel_messages);
                            break;
                        case 4:
                            data = "webview";
                            icon = R.drawable.globe_blue;
                            label = getString(R.string.studip_mobile);
                    }
                    if (data != null) {
                        Intent intent = new Intent(this, HomeActivity.class);
                        intent.setAction("org.studip.unofficial_app.shortcut");
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        intent.setData(Uri.parse(data));
                        ShortcutInfoCompat.Builder b = new ShortcutInfoCompat.Builder(this, data);
                        b.setIntent(intent);
                        b.setIcon(IconCompat.createWithResource(this, icon));
                        b.setShortLabel(label);
                        if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)) {
                            ShortcutManagerCompat.requestPinShortcut(this, b.build(), null);
                        }
                    }
                    return true;
                });
            }
        }
        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener()
        {
            @Override
            public void onTabSelected(TabLayout.Tab tab)
            {
                if (binding.pager.getAdapter() != null && tab.getPosition() < binding.pager.getAdapter().getItemCount()) {
                    binding.pager.setCurrentItem(tab.getPosition());
                }
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab)
            {
                if (binding.tabs.getSelectedTabPosition() == binding.tabs.getTabCount()-1) {
                    tab.select();
                    Intent i = new Intent(a,SettingsActivity.class);
                    startActivity(i);
                }
                if (binding.tabs.getSelectedTabPosition() == binding.tabs.getTabCount()-2) {
                    tab.select();
                    Intent i = new Intent(a,WebViewActivity.class);
                    startActivity(i);
                }
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });
        
        
        HomeFragmentsAdapter ad = new HomeFragmentsAdapter(this);
        
        binding.pager.setAdapter(ad);
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                binding.tabs.selectTab(binding.tabs.getTabAt(position));
            }
        });
        
        if (savedInstanceState == null) {
            handleIntent(getIntent());
        }
    
    
        
        
        
        
        
        /*
        for (ShortcutInfoCompat info : ShortcutManagerCompat.getShortcuts(this, ShortcutManagerCompat.FLAG_MATCH_PINNED)) {
            System.out.println(info.getId());
        }
        */
        
        //ShortcutManagerCompat.removeAllDynamicShortcuts(this);
        //ShortcutManagerCompat.removeDynamicShortcuts(this, Arrays.asList("test"));
        //HashSet<String> cat = new HashSet<>();
        //cat.add("org.studip.unofficial_app.TEXT_SHARE_TARGET");
        //ShortcutManagerCompat.pushDynamicShortcut(this, new ShortcutInfoCompat.Builder(this, "test").setShortLabel("test").setIntent(new Intent(this, DeepLinkActivity.class).setAction(Intent.ACTION_VIEW)).setIcon(IconCompat.createWithResource(this, R.drawable.admin_blue)).setCategories(cat).setActivity(new ComponentName(getPackageName(), getPackageName()+".ui.DeepLinkActivity")).build());
        
        setContentView(binding.getRoot());
    }
    
    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        //System.out.println("new intent");
        handleIntent(intent);
    }
    
    @Override
    protected void onDestroy() {
        super.onDestroy();
        // to prevent redoing the shortcut action when the user leaves with the back button
        if (isFinishing() && ! Intent.ACTION_MAIN.equals(getIntent().getAction())) {
            finishAndRemoveTask();
        }
    }
    
    private void handleIntent(Intent start) {
        //System.out.println("handle intent");
        //System.out.println(start.toString());
        /*
        if (Intent.ACTION_VIEW.equals(start.getAction())) {
            // TODO handle the OAuth callback link, if it is actually used
            
        }
         */
        if (Intent.ACTION_SEND.equals(start.getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(start.getAction()) || Intent.ACTION_SENDTO.equals(start.getAction())) {
            String share = start.getStringExtra(ShareActivity.SHARE_EXTRA);
            if (share == null) {
                Intent i = new Intent();
                i.fillIn(start, 0);
                i.setClass(this, DeepLinkActivity.class);
                finishAndRemoveTask();
                startActivity(i);
                return;
            }
        }
        if ((getPackageName()+".shortcut").equals(start.getAction())) {
            closeAllDialogs(getSupportFragmentManager());
            if ("courses".equals(start.getDataString())) {
                ShortcutManagerCompat.reportShortcutUsed(this, "courses");
                binding.pager.setCurrentItem(1);
            }
            if ("messages".equals(start.getDataString())) {
                ShortcutManagerCompat.reportShortcutUsed(this, "messages");
                binding.pager.setCurrentItem(3);
            }
            if ("files".equals(start.getDataString())) {
                ShortcutManagerCompat.reportShortcutUsed(this, "files");
                binding.pager.setCurrentItem(2);
            }
            if ("webview".equals(start.getDataString())) {
                ShortcutManagerCompat.reportShortcutUsed(this, "webview");
                startActivity(new Intent(this, WebViewActivity.class));
            }
        }
        if ((getPackageName()+".dynamic_shortcut").equals(start.getAction())) {
            closeAllDialogs(getSupportFragmentManager());
            Uri data = start.getData();
            //System.out.println(start.getDataString());
            if ((getPackageName()+".folder").equals(data.getScheme())) {
                HomeActivityViewModel m = new ViewModelProvider(this).get(HomeActivityViewModel.class);
                FileViewModel f = new ViewModelProvider(this).get(FileViewModel.class);
                binding.pager.setCurrentItem(2);
                LiveData<StudipCourse> l = DBProvider.getDB(this).courseDao().observe(data.getQuery());
                l.observe(this, (c) -> {
                    l.removeObservers(this);
                    if (c != null) {
                        f.setFolder(this, data.getHost(), false);
                        m.setFilesCourse(c);
                    }
                });
            }
            if ((getPackageName()+".meeting").equals(data.getScheme())) {
                API api = APIProvider.getAPI(this);
                if (api == null || api.getUserID() == null) {
                    return;
                }
                Intent i = new Intent(this, MeetingsActivity.class);
                i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.putExtra("url", API.HTTPS+api.getHostname()+"/plugins.php/meetingplugin/api/rooms/join/"+data.getQuery()+"/"+data.getHost());
                startActivity(i);
            }
            if ((getPackageName()+".forum").equals(data.getScheme())) {
                binding.pager.setCurrentItem(1);
                Bundle args = new Bundle();
                args.putString("cid", data.getHost());
                CourseForumDialogFragment forum = new CourseForumDialogFragment();
                forum.setArguments(args);
                forum.show(getSupportFragmentManager(), "course_forum");
            }
            if ((getPackageName()+".forum_entry").equals(data.getScheme())) {
                binding.pager.setCurrentItem(1);
                Bundle args = new Bundle();
                args.putString("cid", data.getHost());
                args.putString("entry", data.getQuery());
                CourseForumDialogFragment forum = new CourseForumDialogFragment();
                forum.setArguments(args);
                forum.show(getSupportFragmentManager(), "course_forum");
            }
            if ((getPackageName()+".courseware").equals(data.getScheme())) {
                binding.pager.setCurrentItem(1);
                Bundle args = new Bundle();
                args.putString("cid", data.getHost());
                args.putString("selected", data.getQuery());
                CoursewareDialog courseware = new CoursewareDialog();
                courseware.setArguments(args);
                getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                        .add(android.R.id.content, courseware, "dialog_courseware").addToBackStack(null).commit();
            }
            if ((getPackageName()+".webview").equals(data.getScheme())) {
                binding.pager.setCurrentItem(1);
                Intent i = new Intent(this, WebViewActivity.class);
                i.setAction(Intent.ACTION_VIEW);
                i.setData(Uri.parse(Uri.decode(data.getHost())));
                startActivity(i);
            }
        }
        if ((getPackageName()+".deeplink").equals(start.getAction())) {
            Uri data = start.getData();
            //System.out.println(data.toString());
            if (data != null && !("http".equals(data.getScheme()) || "https".equals(data.getScheme()))) {
                finishAndRemoveTask();
                return;
            }
    
            API api = APIProvider.getAPI(this);
            if (api == null) {
                if (data != null) {
                    Toast.makeText(this, R.string.not_logged_in, Toast.LENGTH_SHORT).show();
                    finish();
                    return;
                }
                Intent intent = new Intent(this, ServerSelectActivity.class);
                startActivity(intent);
                finish();
                return;
            }
            else {
                if (data != null && !api.getHostname().equals(data.getHost())) {
                    finishAndRemoveTask();
                    return;
                }
            }
            if (data != null) {
                boolean handled = false;
                String query = data.getQuery();
                String path = data.getPath();
                if (path == null) {
                    finish();
                    return;
                }
                if (query != null) {
                    path += "?" + query;
                }
                //System.out.println(path);
                if (path.equals("/dispatch.php/start")) {
                    binding.pager.setCurrentItem(0);
                    handled = true;
                }
                if (path.equals("/dispatch.php/my_courses")) {
                    binding.pager.setCurrentItem(1);
                    handled = true;
                }
                if (path.equals("/dispatch.php/files")) {
                    binding.pager.setCurrentItem(2);
                    handled = true;
                }
                if (path.equals("/dispatch.php/messages/overview")) {
                    binding.pager.setCurrentItem(3);
                    handled = true;
                }
                //System.out.println(path);
                Matcher matcher = courseFilesPattern.matcher(path);
                if (matcher.matches()) {
                    HomeActivityViewModel m = new ViewModelProvider(this).get(HomeActivityViewModel.class);
                    FileViewModel f = new ViewModelProvider(this).get(FileViewModel.class);
                    binding.pager.setCurrentItem(2);
                    LiveData<StudipCourse> l = DBProvider.getDB(this).courseDao().observe(matcher.group(2));
                    l.observe(this, (c) -> {
                        l.removeObservers(this);
                        if (c != null) {
                            m.setFilesCourse(c);
                            f.setFolder(this, c.course_id, true);
                        }
                    });
                    handled = true;
                }
                
                matcher = courseFilesPatternFolder.matcher(path);
                if (matcher.matches()) {
                    HomeActivityViewModel m = new ViewModelProvider(this).get(HomeActivityViewModel.class);
                    FileViewModel f = new ViewModelProvider(this).get(FileViewModel.class);
                    binding.pager.setCurrentItem(2);
                    LiveData<StudipCourse> l = DBProvider.getDB(this).courseDao().observe(matcher.group(2));
                    Matcher finalMatcher = matcher;
                    l.observe(this, (c) -> {
                        l.removeObservers(this);
                        if (c != null) {
                            f.setFolder(this, finalMatcher.group(1), false);
                            m.setFilesCourse(c);
                        }
                    });
                    handled = true;
                }
                
                matcher = userFilePattern.matcher(path);
                if (matcher.matches()) {
                    HomeActivityViewModel m = new ViewModelProvider(this).get(HomeActivityViewModel.class);
                    FileViewModel f = new ViewModelProvider(this).get(FileViewModel.class);
                    binding.pager.setCurrentItem(2);
                    m.setFilesCourse(null);
                    f.setFolder(this, matcher.group(1), false);
                    handled = true;
                }
                
                matcher = fileDetailsPattern.matcher(path);
                if (matcher.matches()) {
                    final AppCompatActivity a = this;
                    api.file.get(matcher.group(1)).enqueue(new Callback<StudipFolder.FileRef>()
                    {
                        @Override
                        public void onResponse(@NonNull Call<StudipFolder.FileRef> call, @NonNull Response<StudipFolder.FileRef> response) {
                            StudipFolder.FileRef f = response.body();
                            if (f != null) {
                                api.downloadFile(a, f.id, f.name, false);
                            }
                        }
                        @Override
                        public void onFailure(@NonNull Call<StudipFolder.FileRef> call, @NonNull Throwable t) { }
                    });
                    handled = true;
                }
                
                matcher = messagePattern.matcher(path);
                if (matcher.matches()) {
                    //System.out.println("message");
                    MessagesViewModel m = new ViewModelProvider(this).get(MessagesViewModel.class);
                    m.mes.refresh(this);
                    m.open.setValue(matcher.group(1));
                    binding.pager.setCurrentItem(3);
                    handled = true;
                }
    
                matcher = messagePattern2.matcher(path);
                if (matcher.matches()) {
                    //System.out.println("message");
                    MessagesViewModel m = new ViewModelProvider(this).get(MessagesViewModel.class);
                    m.mes.refresh(this);
                    m.open.setValue(matcher.group(1));
                    binding.pager.setCurrentItem(3);
                    handled = true;
                }
                
                matcher = opencastPattern.matcher(path);
                if (matcher.matches()) {
                    Bundle args = new Bundle();
                    args.putString("cid", matcher.group(1));
                    CourseOpencastDialog opencast = new CourseOpencastDialog();
                    opencast.setArguments(args);
                    opencast.show(getSupportFragmentManager(),"course_opencast");
                    binding.pager.setCurrentItem(1);
                    handled = true;
                }
    
                matcher = courseCoursewarePattern.matcher(path);
                if (matcher.matches()) {
                    Bundle args = new Bundle();
                    args.putString("cid", matcher.group(1));
                    args.putString("selected", matcher.group(3));
                    CoursewareDialog courseware = new CoursewareDialog();
                    courseware.setArguments(args);
                    getSupportFragmentManager().beginTransaction().setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
                            .add(android.R.id.content, courseware, "dialog_courseware").addToBackStack(null).commit();
                    binding.pager.setCurrentItem(1);
                    handled = true;
                }
    
                matcher = meetingsPattern.matcher(path);
                if (matcher.matches()) {
                    Bundle args = new Bundle();
                    args.putString("cid", matcher.group(1));
                    MeetingsRoomsDialog opencast = new MeetingsRoomsDialog();
                    opencast.setArguments(args);
                    opencast.show(getSupportFragmentManager(),"course_meetings");
                    binding.pager.setCurrentItem(1);
                    handled = true;
                }
    
                matcher = meetingsJoinPattern.matcher(path);
                if (matcher.matches()) {
                    Intent i = new Intent(this, MeetingsActivity.class);
                    i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    i.putExtra("url", API.HTTPS+api.getHostname()+"/plugins.php/meetingplugin/api/rooms/join/"+matcher.group(1));
                    startActivity(i);
                    finishAndRemoveTask();
                    handled = true;
                }
                
                matcher = courseForumPattern.matcher(path);
                if (matcher.matches()) {
                    Bundle args = new Bundle();
                    args.putString("cid", matcher.group(1));
                    CourseForumDialogFragment forum = new CourseForumDialogFragment();
                    forum.setArguments(args);
                    forum.show(getSupportFragmentManager(), "course_forum");
                    binding.pager.setCurrentItem(1);
                    handled = true;
                }
    
                matcher = courseForumPattern2.matcher(path);
                if (matcher.matches()) {
                    Bundle args = new Bundle();
                    args.putString("cid", matcher.group(1));
                    CourseForumDialogFragment forum = new CourseForumDialogFragment();
                    forum.setArguments(args);
                    forum.show(getSupportFragmentManager(), "course_forum");
                    binding.pager.setCurrentItem(1);
                    handled = true;
                }
    
                matcher = courseForumEntryPattern.matcher(path);
                if (matcher.matches()) {
                    Bundle args = new Bundle();
                    args.putString("cid", matcher.group(2));
                    args.putString("entry", matcher.group(1));
                    CourseForumDialogFragment forum = new CourseForumDialogFragment();
                    forum.setArguments(args);
                    forum.show(getSupportFragmentManager(), "course_forum");
                    binding.pager.setCurrentItem(1);
                    handled = true;
                }
    
                matcher = courseForumEntryPattern2.matcher(path);
                if (matcher.matches()) {
                    Bundle args = new Bundle();
                    args.putString("cid", matcher.group(2));
                    args.putString("entry", matcher.group(1));
                    CourseForumDialogFragment forum = new CourseForumDialogFragment();
                    forum.setArguments(args);
                    forum.show(getSupportFragmentManager(), "course_forum");
                    binding.pager.setCurrentItem(1);
                    handled = true;
                }
                
                
                
                if (path.equals("/dispatch.php/settings/general")) {
                    Intent i = new Intent(this, SettingsActivity.class);
                    startActivity(i);
                    handled = true;
                }
                
                
                if (!handled && "https".equals(data.getScheme())) {
                    //System.out.println("opening in webview");
                    Intent i = new Intent(this, WebViewActivity.class);
                    i.setAction(Intent.ACTION_VIEW);
                    i.setData(data);
                    startActivity(i);
                }
            }
        }
        //System.out.println(start.getAction());
        //System.out.println(start.getStringExtra(ShareActivity.SHARE_EXTRA));
        if (Intent.ACTION_SEND.equals(start.getAction()) || Intent.ACTION_SEND_MULTIPLE.equals(start.getAction()) 
                || start.getParcelableExtra(Intent.EXTRA_INTENT) != null) {
            Intent extra_int = start.getParcelableExtra(Intent.EXTRA_INTENT);
            if (extra_int != null) {
                start.removeExtra(Intent.EXTRA_INTENT);
                start = extra_int;
            }
            String type = start.getStringExtra(ShareActivity.SHARE_EXTRA);
            if (type != null) {
                closeAllDialogs(getSupportFragmentManager());
                if (type.equals(ShareActivity.SHARE_MESSAGE)) {
                    MessagesViewModel m = new ViewModelProvider(this).get(MessagesViewModel.class);
                    m.source.setValue(start);
                    // TODO handle attachments from SEND_MULTIPLE and SEND by uploading them to the "Wysiwyg Uploads" folder
                    //      and putting a file link at the end
                    
                    binding.pager.setCurrentItem(3);
                    
                }
                if (type.equals(ShareActivity.SHARE_FORUM)) {
                    CoursesViewModel m = new ViewModelProvider(this).get(CoursesViewModel.class);
                    m.forumIntent.setValue(start);
                    binding.pager.setCurrentItem(1);
                }
                if (type.equals(ShareActivity.SHARE_BLUBBER)) {
                    
                }
            }
        }
    }
    
    public static void closeAllDialogs(FragmentManager m) {
        closeDialog(m, "course_forum");
        closeDialog(m, "course_news");
        closeDialog(m, "course_opencast");
        closeDialog(m, "dialog_courseware");
        closeDialog(m, "course_meetings");
        closeDialog(m, "con_lost");
        closeDialog(m, "mkdir_dialog");
        closeDialog(m, "message_write");
        closeDialog(m, "message_view_dialog");
    }
    
    public static void closeDialog(FragmentManager m, String tag) {
        try {
            //noinspection ConstantConditions
            ((DialogFragment) m.findFragmentByTag(tag)).dismiss();
        } catch (Exception ignored) {}
    }
    
    public static void onStatusReturn(FragmentActivity a, int status) {
        HomeActivityViewModel homem = new ViewModelProvider(a).get(HomeActivityViewModel.class);
        if (homem.connectionLostDialogShown.getValue() != null && ! homem.connectionLostDialogShown.getValue()) {
            if (status != -1)
            {
                //System.out.println(status);
                if (status == 401)
                {
                    homem.connectionLostDialogShown.setValue(true);
                    HomeActivity.showConnectionLostDialog(a, true);
                }
                else
                {
                    if (status == 403 || status == 404 || status == 405) {
                        //System.out.println("Route not enabled");
                        return;
                    }
                    if (status != 200)
                    {
                        homem.connectionLostDialogShown.setValue(true);
                        HomeActivity.showConnectionLostDialog(a, false);
                    }
                }
            }
        }
    }
    
    public static void showConnectionLostDialog(FragmentActivity a, boolean autologout) {
        Bundle b = new Bundle();
        b.putBoolean("autologout",autologout);
        ConnectionLostDialogFragment d = new ConnectionLostDialogFragment();
        d.setArguments(b);
        d.show(a.getSupportFragmentManager(),"con_lost");
    }
    
    public static class ConnectionLostDialogFragment extends DialogFragment {
        @NonNull
        @Override
        public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
        {
            Bundle b = getArguments();
            boolean autologout = (b != null) && b.getBoolean("autologout", false);
            AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity());
            builder.setMessage( (autologout) ? R.string.autologout_msg : 
                    R.string.con_lost_msg);
            builder.setTitle(R.string.con_lost_title);
            if (autologout) {
                builder.setPositiveButton(R.string.autologout_relogin, (dialog, which) ->
                {
                    Intent i = new Intent(requireActivity(),LoginActivity.class);
                    startActivity(i);
                    dismiss();
                }).setNegativeButton(R.string.continue_msg,(a,c) -> dismiss());
            } else {
                builder.setNegativeButton(R.string.continue_msg,(a,c) -> dismiss());
            }
            return builder.create();
        }
    }
    
    public void navigateTo(int position) {
        binding.pager.setCurrentItem(position);
    }
    
    private static class HomeFragmentsAdapter extends FragmentStateAdapter {

        public HomeFragmentsAdapter(@NonNull FragmentActivity fragmentActivity)
        {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position)
        {
            switch (position) {
                case 3:
                    return new MessageFragment();
                case 2:
                    return new FileFragment();
                case 1:
                    return new CoursesFragment();
                case 0:
                default:
                    return new HomeFragment();
            }
        }

        @Override
        public int getItemCount()
        {
            return 4;
        }
    }
    
    
    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        switch (level) {
            case ComponentCallbacks2.TRIM_MEMORY_UI_HIDDEN:

                
            
                break;
        
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_LOW:
            case ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL:

                /*
                   Release any memory that your app doesn't need to run.

                   The device is running low on memory while the app is running.
                   The event raised indicates the severity of the memory-related event.
                   If the event is TRIM_MEMORY_RUNNING_CRITICAL, then the system will
                   begin killing background processes.
                */
            
                break;
        
            case ComponentCallbacks2.TRIM_MEMORY_BACKGROUND:
            case ComponentCallbacks2.TRIM_MEMORY_MODERATE:
            case ComponentCallbacks2.TRIM_MEMORY_COMPLETE:

                /*
                   Release as much memory as the process can.

                   The app is on the LRU list and the system is running low on memory.
                   The event raised indicates where the app sits within the LRU list.
                   If the event is TRIM_MEMORY_COMPLETE, the process will be one of
                   the first to be terminated.
                */
            
                break;
        
            default:
                /*
                  Release any non-critical data structures.

                  The app received an unrecognized memory level value
                  from the system. Treat this as a generic low-memory message.
                */
                break;
        }
        //System.out.println("memory trim, run GC");
        System.gc();
    }
    
    
}
