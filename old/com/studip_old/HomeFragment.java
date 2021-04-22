package com.studip_old;import org.studip.unofficial_app.R;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.studip_old.api.ManagedObjectListener;
import com.studip_old.api.News;
import com.studip_old.api.NewsList;
import com.studip_old.api.rest.StudipListObject;
import com.studip_old.api.rest.StudipNews;
import com.studip_old.api.rest.StudipUser;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    private NewsAdapter ad;
    Handler h;
    private Callback listener = new Callback();
    private UserCallback user_listener = new UserCallback();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        SwipeRefreshLayout r = v.findViewById(R.id.home_refresh);
        r.setOnRefreshListener(this);
        ListView l = v.findViewById(R.id.home_list);
        if (Data.user == null)
        {
            Data.user_provider.addRefreshListener(user_listener);
            Data.user_provider.refresh();
        }
        ad = new NewsAdapter(getActivity(),ArrayAdapter.NO_SELECTION);
        l.setAdapter(ad);
        try
        {
            StudipUser u = Data.user;
            if (u != null && u.name != null && u.name.given != null)
            {
                String welcome_message = getString(R.string.welcome)+" "+u.name.given+"!";
                TextView welcome = v.findViewById(R.id.welcome_message);
                welcome.setText(welcome_message);
            }
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        h = HandlerCompat.createAsync(Looper.getMainLooper());
        
        if (Data.global_news == null)
        {
            Data.global_news = new NewsList(h,null);
            Data.global_news.addRefreshListener(listener);
            Data.global_news.refresh();
        }
        else
        {
            Data.global_news.addRefreshListener(listener);
        }
        
        
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState != null)
        {
            // simulate a refresh
            listener.callback(Data.global_news.getData(),null);
        }
    }

    private class Callback extends ManagedObjectListener<StudipListObject>
    {
        @Override
        public void callback(StudipListObject obj, Exception error)
        {
            try
            {
                StudipNews[] news = News.ArrayFromList(Data.global_news.getData());
                ad.news = news;
                ad.notifyDataSetChanged();
            } catch (Exception e)
            {
                e.printStackTrace();
            }
            ListView l = getView().findViewById(R.id.home_list);
            SwipeRefreshLayout r = getView().findViewById(R.id.home_refresh);
            r.setRefreshing(false);
        }
    }
    
    private class UserCallback extends ManagedObjectListener<StudipUser>
    {
        @Override
        public void callback(StudipUser obj, Exception error)
        {
            Data.user_provider.removeRefreshListener(this);
            StudipUser u = Data.user;
            if (u != null && u.name != null && u.name.given != null)
            {
                String welcome_message = getString(R.string.welcome)+" "+u.name.given+"!";
                TextView welcome = getView().findViewById(R.id.welcome_message);
                welcome.setText(welcome_message);
            }
        }
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Data.global_news.removeRefreshListener(listener);
    }
    
    
    
    
    
    
    @Override
    public void onRefresh()
    {
        Data.global_news.refresh();
    }
}