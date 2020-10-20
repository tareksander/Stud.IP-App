package com.studip;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.studip.api.API;
import com.studip.api.Messages;
import com.studip.api.rest.StudipListObject;
import com.studip.api.rest.StudipMessage;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class MessagesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Runnable
{
    MessagesAdapter adapter;
    Handler h;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);
        
        
        // route: /user/:user_id/inbox
        // HEAD request doesn't seem to work

        h = HandlerCompat.createAsync(Looper.getMainLooper());
        if (Data.messages_provider == null)
        {
            Data.messages_provider = new Messages(h);
            Data.messages_provider.addRefreshListener(this);
        }
        
        
        SwipeRefreshLayout ref = v.findViewById(R.id.messages_refresh);
        ref.setOnRefreshListener(this);
        
        
        ListView l = v.findViewById(R.id.messages_list);
        adapter = new MessagesAdapter(getActivity(),ArrayAdapter.NO_SELECTION);
        l.setAdapter(adapter);
        
        return v;
    }



    @Override
    public void run()
    {
        
        StudipListObject l  = Data.messages_provider.getData();
        Iterator<Map.Entry<String, JsonElement>> it = l.collection.entrySet().iterator();
        Data.messages = new StudipMessage[l.collection.size()];
        int index = 0;
        while (it.hasNext())
        {
            JsonElement e = it.next().getValue();
            try
            {
                Data.messages[index] = Data.gson.fromJson(e,StudipMessage.class);
            } catch (JsonSyntaxException ignored) {Data.messages = null; return;};
            index++;
        }
        
        
    }
    
    

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null)
        {
            if (Data.messages == null)
            {
                Data.messages_provider.refresh();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Data.messages_provider.removeRefreshListener(this);
    }


    @Override
    public void onRefresh()
    {
        
    }

    
    
    public class MessagesAdapter extends ArrayAdapter
    {
        public MessagesAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }
        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            return super.getView(position, convertView, parent);
        }

        @Override
        public int getCount()
        {
            return super.getCount();
        }
    }
    
    
    
    
    
    
    
    
    
    
}