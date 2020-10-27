package com.studip;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Instrumentation;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.MimeTypeMap;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.app.ActivityCompat;
import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.studip.api.API;
import com.studip.api.ByteRouteCallback;
import com.studip.api.Folder;
import com.studip.api.RouteCallback;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

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
            // TODO defer until the data has been restored
            Data.folder_provider = new Folder(HandlerCompat.createAsync(Looper.getMainLooper()), Folder.TYPE_USER_TOP_FOLDER,Data.user.user_id);
        }
        Data.folder_provider.addRefreshListener(this);
        if (savedInstanceState == null)
        {
            Data.folder_provider.refresh();
            //System.out.println("refreshing, new instance");
        }

        Downloads.initDownloads(getActivity());
        
        getView().findViewById(R.id.button_mkdir).setOnClickListener(this::onMkdir);
        getView().findViewById(R.id.button_upload).setOnClickListener(this::onUpload);
        
        
       
        
         

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
    
    
    
    
    public void onMkdir(View v)
    {
        if (Data.current_folder == null)
        {
            return;
        }
        EditText ed = new EditText(getActivity());
        ed.setSingleLine(true);
        new AlertDialog.Builder(getActivity()).setTitle(R.string.mkdir).setView(ed).setCancelable(true).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (! ed.getText().toString().equals(""))
                {
                    SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
                    ref.setRefreshing(true);
                    try
                    {
                        Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new FolderRoute("new_folder", API.METHOD_POST, Data.current_folder.id,"name="+URLEncoder.encode(ed.getText().toString(),"UTF-8")), new RouteCallback()
                        {
                            @Override
                            public void routeFinished(String result, Exception error)
                            {
                                if (error != null)
                                {
                                    error.printStackTrace();
                                }
                                Data.folder_provider.refresh();
                            }
                        }));
                    }
                    catch (UnsupportedEncodingException e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }).show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode,resultCode,data);
        if (data != null)
        {
            Uri file = data.getData();
            SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
            ref.setRefreshing(true);
            //System.out.println(file.toString());
            try (ByteArrayOutputStream bytes = new ByteArrayOutputStream(1000000))
            {
                InputStream r = getActivity().getContentResolver().openInputStream(file);
                byte[] tmp = new byte[100];
                int read;
                while ((read = r.read(tmp)) != -1)
                {
                    bytes.write(tmp,0,read);
                }
                byte[] res = bytes.toByteArray();
                //System.out.println((char) res[1]);
                //System.out.println("data fetched!");
                Data.api.submitWithByteCallback(Data.api.new CallbackByteRoute(Data.api.new UploadFileRoute(Data.current_folder.id, API.formatUploadData(Downloads.getFileName(file, getActivity()), res)), new ByteRouteCallback()
                {
                    @Override
                    public void routeFinished(byte[] result, Exception error)
                    {
                        Data.folder_provider.refresh();
                    }
                }));
                bytes.close();
            } catch (Exception ignored) {
                ref.setRefreshing(false);
            }
            //System.out.println("file: "+file.toString());
        }
    }

    public void onUpload(View v)
    {
        if (Data.current_folder == null)
        {
            return;
        }
        Intent intent = new Intent().setType("*/*").setAction(Intent.ACTION_OPEN_DOCUMENT);
        if (intent.resolveActivity(getActivity().getPackageManager()) != null)
        {
            startActivityForResult(Intent.createChooser(intent, getResources().getString(R.string.select_file)), 123);
        }
    }
    
    private class FileClicked extends ByteRouteCallback implements View.OnClickListener, View.OnLongClickListener
    {
        String id;
        String name;
        public FileClicked(String id,String name)
        {
            this.id = id;
            this.name = name;
        }
        @Override
        public void onClick(View v)
        {
             SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
             ref.setRefreshing(true);
             Data.api.submitWithByteCallback(Data.api.new CallbackByteRoute(Data.api.new DownloadFileRoute(id),this));
        }

        @Override
        public void routeFinished(byte[] result, Exception error)
        {
            SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
            ref.setRefreshing(false);
            //System.out.println((char)  result[1]);
            //System.out.println("download finished")
            //System.out.println(result.length);
            if (result != null)
            {
                try
                {
                    OutputStream out = Downloads.openInDownloads(getActivity(),name);
                    out.write(result);
                    out.flush();
                    out.close();
                    //System.out.println("file written");
                } catch (Exception ignored) {ignored.printStackTrace();}
            }
            if (error != null)
            {
                error.printStackTrace();
            }
        }

        @Override
        public boolean onLongClick(View v)
        {
            Resources res = getResources();
            new AlertDialog.Builder(getActivity()).setTitle(R.string.delete).setMessage(res.getString(R.string.file)+": "+name+"\n"+res.getString(R.string.delete_msg)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
                    ref.setRefreshing(true);
                    Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new FileRoute("", API.METHOD_DELETE, id), new RouteCallback()
                    {
                        @Override
                        public void routeFinished(String result, Exception error)
                        {
                            Data.folder_provider.refresh();
                        }
                    }));
                }
            }).show();
            return true;
        }
    }
    
    
    

    private class FolderClicked implements View.OnClickListener, View.OnLongClickListener
    {
        final String id;
        final String name;
        public FolderClicked(String id,String name)
        {
            this.id = id;
            this.name = name;
        }
        @Override
        public void onClick(View v)
        {
            SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
            ref.setRefreshing(true);
            Data.folder_provider.reinit(Folder.TYPE_FOLDER,id);
            Data.folder_provider.refresh();
        }
        @Override
        public boolean onLongClick(View v)
        {
            Resources res = getResources();
            new AlertDialog.Builder(getActivity()).setTitle(R.string.delete).setMessage(res.getString(R.string.dir)+": "+name+"\n"+res.getString(R.string.delete_msg)).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
                    ref.setRefreshing(true);
                    Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new FolderRoute("", API.METHOD_DELETE, id), new RouteCallback()
                    {
                        @Override
                        public void routeFinished(String result, Exception error)
                        {
                            Data.folder_provider.refresh();
                        }
                    }));
                }
            }).show();
            return true;
        }
    }

    private class ParentLongClicked implements  View.OnLongClickListener
    {
        @Override
        public boolean onLongClick(View v)
        {
            SwipeRefreshLayout ref = getView().findViewById(R.id.file_refresh);
            ref.setRefreshing(true);
            Data.folder_provider.reinit(Folder.TYPE_USER_TOP_FOLDER,Data.user.user_id);
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
                v.setClickable(true);
            }
            if (position == 0)
            {
                // entry for the parent folder
                v.setText("..");
                if (! Data.current_folder.parent_id.equals(""))
                {
                    v.setOnClickListener(new FolderClicked(Data.current_folder.parent_id,"")); // name is irrelevant, since a parent folder can't be deleted
                }
                v.setOnLongClickListener(parent_long_clicked_listener);
                v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_empty_blue,0,0,0);
                return v;
            }
            position--;
            if (position < Data.current_folder.subfolders.length)
            {
                // entries for the folders
                v.setText(Data.current_folder.subfolders[position].name);
                FolderClicked c = new FolderClicked(Data.current_folder.subfolders[position].id,Data.current_folder.subfolders[position].name);
                v.setOnClickListener(c);
                v.setOnLongClickListener(c);
                v.setCompoundDrawablesWithIntrinsicBounds(R.drawable.folder_empty_blue,0,0,0);
                return v;
            }
            position -= Data.current_folder.subfolders.length;
            // entries for the files
            v.setText(Data.current_folder.file_refs[position].name);
            FileClicked c = new FileClicked(Data.current_folder.file_refs[position].id,Data.current_folder.file_refs[position].name);
            v.setOnClickListener(c);
            v.setOnLongClickListener(c);
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