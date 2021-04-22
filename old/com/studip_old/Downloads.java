package com.studip_old;import org.studip.unofficial_app.R;
import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;

import androidx.core.app.ActivityCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
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
            Cursor c = res.query(MediaStore.Downloads.EXTERNAL_CONTENT_URI,null,null,null);
            int nameindex = c.getColumnIndex(MediaStore.Downloads.DISPLAY_NAME);
            int id = c.getColumnIndex(MediaStore.Downloads._ID);
            c.moveToFirst();
            for (int i = 0;i<c.getCount();i++)
            {
                if (c.getString(nameindex).equals(name))
                {
                    int fileid = c.getInt(id);
                    //System.out.println("file found!: "+ c.getString(nameindex));
                    c.close();
                    return res.openOutputStream(ContentUris.withAppendedId(MediaStore.Downloads.EXTERNAL_CONTENT_URI,fileid));
                }
                c.moveToNext();
            }
            c.close();
            
            ContentValues vals = new ContentValues();
            //vals.put(MediaStore.Downloads.RELATIVE_PATH,Environment.DIRECTORY_DOWNLOADS);
            vals.put(MediaStore.Downloads.DISPLAY_NAME,name);
            
            Uri uri = res.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI,vals);
            //System.out.println(uri.toString());
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
    public static String getFileName(Uri uri,Activity a)
    {
        String result = null;
        if (uri.getScheme().equals("content"))
        {
            Cursor cursor = a.getContentResolver().query(uri, null, null, null, null);
            try
            {
                if (cursor != null && cursor.moveToFirst())
                {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
            finally
            {
                cursor.close();
            }
        }
        if (result == null)
        {
            result = uri.getPath();
            int cut = result.lastIndexOf('/');
            if (cut != -1)
            {
                result = result.substring(cut + 1);
            }
        }
        return result;
    }
}