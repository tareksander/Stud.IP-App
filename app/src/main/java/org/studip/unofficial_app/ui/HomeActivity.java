package org.studip.unofficial_app.ui;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipCourse;
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
import org.studip.unofficial_app.model.viewmodels.FileViewModel;
import org.studip.unofficial_app.model.viewmodels.HomeActivityViewModel;
import org.studip.unofficial_app.model.viewmodels.MessagesViewModel;
import org.studip.unofficial_app.ui.fragments.CoursesFragment;
import org.studip.unofficial_app.ui.fragments.FileFragment;
import org.studip.unofficial_app.ui.fragments.HomeFragment;
import org.studip.unofficial_app.ui.fragments.MessageFragment;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeActivity extends AppCompatActivity
{
    public static final Pattern courseFilesPattern = Pattern.compile("/dispatch\\.php/course/files?(/index)\\?cid=(\\p{Alnum}+)$");
    public static final Pattern courseFilesPatternFolder = Pattern.compile("/dispatch\\.php/course/files/index/(\\p{Alnum}+)\\?cid=(\\p{Alnum}+)$");
    
    
    public static final Pattern courseForumPattern = Pattern.compile("/plugins\\.php/coreforum/index\\?cid=(\\p{Alnum}+)$");
    
    
    public static final Pattern courseMembersPattern = Pattern.compile("/dispatch\\.php/course/members\\?cid=(\\p{Alnum}+)$");

    public static final Pattern courseCoursewarePattern = Pattern.compile("/plugins\\.php/courseware/courseware\\?cid=(\\p{Alnum}+)?(&selected=(\\p{Alnum}+))$");
    
    public static final Pattern messagePattern = Pattern.compile("/dispatch.php/messages/read/(\\p{Alnum}+)");
    
    
    
    
    private ActivityHomeBinding binding;
    @SuppressLint("CheckResult")
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        
        
        binding = ActivityHomeBinding.inflate(getLayoutInflater());

        Notifications.initChannels(this);

        Settings s = SettingsProvider.getSettings(this);
        AppCompatDelegate.setDefaultNightMode(SettingsProvider.getSettings(this).theme);
        if (s.logout) {
            s.logout = false;
            System.out.println("clearing database");
            s.safe(SettingsProvider.getSettingsPreferences(this));
            DB db = DBProvider.getDB(this);
            db.getTransactionExecutor().execute(() -> {
                db.clearAllTables();
                System.out.println("database cleared");
            });
            DocumentsDB docdb = DocumentsDBProvider.getDB(this);
            docdb.getTransactionExecutor().execute(() -> {
                docdb.clearAllTables();
                System.out.println("document database cleared");
            });
            if (s.notification_service_enabled) {
                NotificationWorker.enqueue(this);
            }
            Intent intent = new Intent(this,ServerSelectActivity.class);
            startActivity(intent);
            finish();
            return;
        }
    
        

        Uri data = getIntent().getData();
        
        if (data != null && ! ("http".equals(data.getScheme()) || "https".equals(data.getScheme()))) {
            finish();
            return;
        }
        
        API api = APIProvider.getAPI(this);
        if (api == null)
        {
            if (data != null) {
                Toast.makeText(this, R.string.not_logged_in, Toast.LENGTH_SHORT).show();
                finish();
                return;
            }
            Intent intent = new Intent(this,ServerSelectActivity.class);
            startActivity(intent);
            finish();
            return;
        } else {
            if (data != null && ! api.getHostname().equals(data.getHost())) {
                finish();
                return;
            }
        }
        
        Activity a = this;
        
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


        if (data != null) {
            // TODO handle the studip links
            String query = data.getQuery();
            String path = data.getPath();
            if (path == null) {
                finish();
                return;
            }
            if (query != null) {
                path  += "?"+query;
            }
            if (path.equals("/dispatch.php/start")) {
                binding.pager.setCurrentItem(0);
            }
            if (path.equals("/dispatch.php/my_courses")) {
                binding.pager.setCurrentItem(1);
            }
            if (path.equals("/dispatch.php/files")) {
                binding.pager.setCurrentItem(2);
            }
            if (path.equals("/dispatch.php/messages/overview")) {
                binding.pager.setCurrentItem(3);
            }
            //System.out.println(path);
            Matcher matcher = courseFilesPattern.matcher(path);
            if (matcher.matches()) {
                HomeActivityViewModel m = new ViewModelProvider(this).get(HomeActivityViewModel.class);
                binding.pager.setCurrentItem(2);
                LiveData<StudipCourse> l = DBProvider.getDB(this).courseDao().observe(matcher.group(2));
                l.observe(this,(c) -> {
                    l.removeObservers(this);
                    if (c != null) {
                        m.setFilesCourse(c);
                    }
                });
            }
            matcher = courseFilesPatternFolder.matcher(path);
            if (matcher.matches()) {
                HomeActivityViewModel m = new ViewModelProvider(this).get(HomeActivityViewModel.class);
                FileViewModel f = new ViewModelProvider(this).get(FileViewModel.class);
                binding.pager.setCurrentItem(2);
                LiveData<StudipCourse> l = DBProvider.getDB(this).courseDao().observe(matcher.group(2));
                Matcher finalMatcher = matcher;
                l.observe(this,(c) -> {
                    l.removeObservers(this);
                    if (c != null) {
                        m.setFilesCourse(c);
                        f.setFolder(this, finalMatcher.group(1),false);
                    }
                });
            }
            matcher = messagePattern.matcher(path);
            if (matcher.matches()) {
                new ViewModelProvider(this).get(MessagesViewModel.class).mes.refresh(this);
                binding.pager.setCurrentItem(3);
            }
            
            
            if (path.equals("/dispatch.php/settings/general") || path.equals("")) {
                Intent i = new Intent(a,SettingsActivity.class);
                startActivity(i);
            }
        }
        
        
        
        
        setContentView(binding.getRoot());
    }
    
    public static void onStatusReturn(FragmentActivity a,int status) {
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
            builder.setMessage( (autologout) ? R.string.autologout_msg : R.string.con_lost_msg);
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
    
    
    
    
    
}
