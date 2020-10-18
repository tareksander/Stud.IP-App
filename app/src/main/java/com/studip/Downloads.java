package com.studip;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;

import androidx.core.app.ActivityCompat;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;

public class Downloads
{
    
    
    // Requests the permission from the user for api level <= 28
    public static void initDownloads(Activity a)
    {
        if (Build.VERSION.SDK_INT <= 28)
        {
            ActivityCompat.requestPermissions(a, new String[] {Manifest.permission.WRITE_EXTERNAL_STORAGE},0);
        }
    }
    
    
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public static OutputStream openInDownloads(Activity a, String name) throws IOException
    {
        if (Build.VERSION.SDK_INT > 28)
        {
            // uses the scoped storage api, use mediastore
            ContentResolver res = a.getContentResolver();
            ContentValues vals = new ContentValues();
            vals.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);
            vals.put(MediaStore.Downloads.DISPLAY_NAME,name);
            
            Uri uri = res.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,vals);
            return res.openOutputStream(uri);
        }
        else
        {
            // uses the old storage api, request external storage permissions
            File downloads = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
            downloads.mkdirs();
            return new FileOutputStream(new File(downloads.toString()+File.separator+name));
        }
    }
    
    
    
    
    
    
    
    
    
    
    
    
}
