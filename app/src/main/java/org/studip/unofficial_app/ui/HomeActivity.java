package org.studip.unofficial_app.ui;

import android.app.Activity;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;

import org.studip.unofficial_app.R;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.databinding.ActivityHomeBinding;
import org.studip.unofficial_app.model.APIProvider;
import org.studip.unofficial_app.model.DBProvider;
import org.studip.unofficial_app.model.NotificationWorker;
import org.studip.unofficial_app.model.Settings;
import org.studip.unofficial_app.model.SettingsProvider;
import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.model.viewmodels.HomeActivityViewModel;
import org.studip.unofficial_app.ui.fragments.CoursesFragment;
import org.studip.unofficial_app.ui.fragments.CoursesNavHostFragment;
import org.studip.unofficial_app.ui.fragments.HomeFragment;

import java.util.function.ObjIntConsumer;

public class HomeActivity extends AppCompatActivity
{
    private ActivityHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        

        Settings s = SettingsProvider.getSettings(this);
        AppCompatDelegate.setDefaultNightMode(SettingsProvider.getSettings(this).theme);
        if (s.logout) {
            s.logout = false;
            System.out.println("clearing database");
            s.safe(SettingsProvider.getSettingsPreferences(this));
            DB db = DBProvider.getDB(this);
            db.getTransactionExecutor().execute(db::clearAllTables);
            if (s.notification_service_enabled) {
                NotificationWorker.enqueue(this);
            }
            Intent intent = new Intent(this,ServerSelectActivity.class);
            startActivity(intent);
            finish();
            return;
        }
        
        
        
        API api = APIProvider.loadAPI(this);
        if (api == null)
        {
            Intent intent = new Intent(this,ServerSelectActivity.class);
            startActivity(intent);
            finish();
            return;
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


        binding.pager.setAdapter(new HomeFragmentsAdapter(this));
        binding.pager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback()
        {
            @Override
            public void onPageSelected(int position)
            {
                super.onPageSelected(position);
                binding.tabs.selectTab(binding.tabs.getTabAt(position));
            }
        });

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
            return 3;
        }
    }
    
    
    
    
    
}
