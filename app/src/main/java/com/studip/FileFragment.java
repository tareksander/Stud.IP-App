package com.studip;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.studip.api.Folder;

public class FileFragment extends Fragment implements Runnable, SwipeRefreshLayout.OnRefreshListener
{
    
    private ParentLongClicked parent_long_clicked_listener;
    FileAdapter file_adapter;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.fragment_file, container, false);
    }
    
    
    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        file_adapter = new FileAdapter(getActivity(),ArrayAdapter.NO_SELECTION);
        Data.filefragment = this;
        
        parent_long_clicked_listener = new ParentLongClicked();
        
        SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
        ref.setOnRefreshListener(this);
        
        if (Data.folder_provider == null)
        {
            Data.folder_provider = new Folder(HandlerCompat.createAsync(Looper.getMainLooper()), Folder.TYPE_USER_TOP_FOLDER,Data.user.getData().user_id);
        }
        Data.folder_provider.addRefreshListener(this);
        if (savedInstanceState == null)
        {
            Data.folder_provider.refresh();
            //System.out.println("refreshing, new instance");
        }
        
        ListView l = getView().findViewById(R.id.file_list);
        l.setAdapter(file_adapter);
    }


    @Override
    public void run()
    {
        Data.current_folder = Data.folder_provider.getData();
        file_adapter.notifyDataSetChanged();
        SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
        ref.setRefreshing(false);
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Data.folder_provider.removeRefreshListener(this);
    }

    @Override
    public void onRefresh()
    {
        Data.folder_provider.refresh();
    }


    private class FileClicked implements View.OnClickListener
    {
        String id;
        public FileClicked(String id)
        {
            this.id = id;
        }
        @Override
        public void onClick(View v)
        {
            
        }
    }

    private class FolderClicked implements View.OnClickListener
    {
        String id;
        public FolderClicked(String id)
        {
            this.id = id;
        }
        @Override
        public void onClick(View v)
        {
            SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
            ref.setRefreshing(true);
            Data.folder_provider.reinit(Folder.TYPE_FOLDER,id);
            Data.folder_provider.refresh();
        }
    }

    private class ParentLongClicked implements  View.OnLongClickListener
    {
        @Override
        public boolean onLongClick(View v)
        {
            SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
            ref.setRefreshing(true);
            Data.folder_provider.reinit(Folder.TYPE_USER_TOP_FOLDER,Data.user.getData().user_id);
            Data.folder_provider.refresh();
            return false;
        }
    }
    
    
    private class FileAdapter extends ArrayAdapter
    {
        public FileAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }

        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            TextView v;
            if (convertView != null)
            {
                v = (TextView) convertView;
            }
            else
            {
                v = new TextView(getActivity());
            }
            if (position == 0)
            {
                // entry for the parent folder
                v.setText("..");
                if (! Data.current_folder.parent_id.equals(""))
                {
                    v.setOnClickListener(new FolderClicked(Data.current_folder.parent_id));
                    v.setOnLongClickListener(parent_long_clicked_listener);
                }
                v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_empty_blue,0,0,0);
                return v;
            }
            position--;
            if (position < Data.current_folder.subfolders.length)
            {
                // entries for the folders
                v.setText(Data.current_folder.subfolders[position].name);
                v.setOnClickListener(new FolderClicked(Data.current_folder.subfolders[position].id));
                v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_empty_blue,0,0,0);
                return v;
            }
            position -= Data.current_folder.subfolders.length;
            // entries for the files
            v.setText(Data.current_folder.file_refs[position].name);
            v.setOnClickListener(new FileClicked(Data.current_folder.file_refs[position].id));
            v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.file_blue,0,0,0);
            return v;
        }
        

        @Override
        public int getCount()
        {
            if (Data.current_folder == null)
            {
                return 0;
            }
            else
            {
                return 1+Data.current_folder.subfolders.length+Data.current_folder.file_refs.length;
            }
        }
        
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
}