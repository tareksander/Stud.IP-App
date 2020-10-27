package com.studip;

import android.content.Context;
import android.graphics.Color;
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

import com.google.gson.JsonObject;
import com.studip.api.News;
import com.studip.api.NewsList;
import com.studip.api.ResponseParser;
import com.studip.api.rest.StudipNews;
import com.studip.api.rest.StudipUser;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Runnable
{
    private HomeAdapter ad;
    Handler h;
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
            Data.user_provider.addRefreshListener(this);
            Data.user_provider.refresh();
        }
        ad = new HomeAdapter(getActivity(),ArrayAdapter.NO_SELECTION);
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
            Data.global_news.addRefreshListener(this::OnNewsRefresh);
            Data.global_news.refresh();
        }
        else
        {
            
        }
        Data.global_news.addRefreshListener(this::OnNewsRefresh);
        
        
        return v;
    }
    
    
    
    public void OnNewsRefresh()
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
    }


    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Data.global_news.removeRefreshListener(this::OnNewsRefresh);
    }

    @Override
    public void run()
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
    
    private class HomeAdapter extends ArrayAdapter
    {
        StudipNews[] news;
        public HomeAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }
        
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            View v;
            if (convertView != null)
            {
                v = convertView;
            }
            else
            {
                v = getLayoutInflater().inflate(R.layout.course_news_entry,parent,false);
            }
            TextView title = v.findViewById(R.id.news_title);
            TextView content = v.findViewById(R.id.news_content);
            title.setText(news[position].topic);
            Document d = Jsoup.parse(news[position].body_html);
            content.setText(d.wholeText());
            return v;
        }

        @Override
        public int getCount()
        {
            if (news == null)
            {
                return 0;
            }
            return news.length;
        }
    }
    
    
    
    @Override
    public void onRefresh()
    {
        
        ListView l = getView().findViewById(R.id.home_list);
        SwipeRefreshLayout r = getView().findViewById(R.id.home_refresh);
        r.setRefreshing(false);
    }
}