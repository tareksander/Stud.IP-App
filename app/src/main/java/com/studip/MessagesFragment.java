package com.studip;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class MessagesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Runnable
{
    
    
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_messages, container, false);
        
        
        
        
        
        
        return v;
    }


    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null)
        {
            
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        
    }


    @Override
    public void onRefresh()
    {
        
    }

    @Override
    public void run()
    {

    }
    
    
    
    
    
    
    
    
    
    
    
    
}