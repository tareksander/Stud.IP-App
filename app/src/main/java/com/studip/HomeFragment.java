package com.studip;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonObject;
import com.studip.api.ResponseParser;

public class HomeFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_home, container, false);
        SwipeRefreshLayout r = v.findViewById(R.id.home_refresh);
        r.setOnRefreshListener(this);
        try
        {
            ListView l = v.findViewById(R.id.home_list);
            
            String json = Data.api.getUserdata().get();
            JsonObject tree = ResponseParser.parseQuery(json);
            String firstname = ResponseParser.getValue(tree,"given");
            String welcome = getString(R.string.welcome);
            String welcome_message = welcome+" "+firstname+"!";
            l.setAdapter(new HomeAdapter(getActivity(),ArrayAdapter.NO_SELECTION,new String[] {welcome_message}));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        return v;
    }

    

    private class HomeAdapter extends ArrayAdapter
    {
        String[] s;
        public HomeAdapter(@NonNull Context context, int resource,String[] s)
        {
            super(context, resource);
            this.s = s;
        }
        
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            
            TextView t;
            if (convertView != null)
            {
                t = (TextView) convertView;
            }
            else
            {
                t = new TextView(getActivity());
            }
            t.setText(s[position]);
            return t;
        }

        @Override
        public int getCount()
        {
            return s.length;
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