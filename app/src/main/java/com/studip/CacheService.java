package com.studip;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;

import androidx.annotation.NonNull;
import androidx.security.crypto.EncryptedFile;
import androidx.security.crypto.MasterKey;
import androidx.security.crypto.MasterKeys;

import com.studip.api.rest.StudipCourse;
import com.studip.api.rest.StudipUser;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.security.GeneralSecurityException;

public class CacheService extends Service
{
    public static final String RESTORE_KEY = "restore";
    public static final int RESTORE = 1;
    public static final int SAVE = 0;
    
    private HandlerThread thread;
    private ServiceHandler h;
    private CacheService instance;
    private class ServiceHandler extends Handler
    {
        public ServiceHandler(Looper looper)
        {
            super(looper);
        }

        @Override
        public void handleMessage(@NonNull Message msg)
        {
            try
            {
                MasterKey.Builder b = new MasterKey.Builder(instance);
                b.setKeyScheme(MasterKey.KeyScheme.AES256_GCM);
                MasterKey m = b.build();
                File file = new File(instance.getCacheDir(), "cached_data");
                if (msg.arg2 == SAVE)
                {
                    file.delete();
                }
                EncryptedFile enc = new EncryptedFile.Builder(instance, file, m, EncryptedFile.FileEncryptionScheme.AES256_GCM_HKDF_4KB).build();
                if (msg.arg2 == RESTORE)
                {
                    try (BufferedReader r = new BufferedReader(new InputStreamReader(enc.openFileInput())))
                    {
                        String s = r.readLine();
                        StudipUser user;
                        if (s.equals("null"))
                        {
                            user = null;
                        }
                        else
                        {
                            user = Data.gson.fromJson(r.readLine(), StudipUser.class);
                            //System.out.println("restored user data");
                        }
                        Data.user = user;
                        StudipCourse[] courselist;
                        s = r.readLine();
                        if (s.equals("null"))
                        {
                            courselist = null;
                        }
                        else
                        {
                            courselist = new StudipCourse[Integer.parseInt(s)];
                            for (int i = 0; i < courselist.length; i++)
                            {
                                courselist[i] = Data.gson.fromJson(r.readLine(), StudipCourse.class);
                            }
                            //System.out.println("restored course list");
                        }
                        Data.courselist = courselist;
                        boolean[] hasevents;
                        s = r.readLine();
                        if (s.equals("null"))
                        {
                            Data.courses_hasevents = null;
                        }
                        else
                        {
                            hasevents = new boolean[Integer.parseInt(s)];
                            for (int i = 0; i < courselist.length; i++)
                            {
                                hasevents[i] = Boolean.parseBoolean(r.readLine());
                            }
                            Data.courses_hasevents = hasevents;
                        }
                    } catch (Exception ignored) {}
                }
                else
                {
                    //System.out.println("saving data");
                    try (BufferedWriter w = new BufferedWriter(new OutputStreamWriter(enc.openFileOutput())))
                    {
                        StudipUser user = Data.user;
                        if (user != null)
                        {
                            w.newLine();
                            w.write(Data.gson.toJson(user));
                            w.newLine();
                            //System.out.println("user saved");
                        }
                        else
                        {
                            //System.out.println("user not available");
                            w.write("null");
                            w.newLine();
                        }
                        StudipCourse[] courselist = Data.courselist; // local reference, so we don't get inconsistent state when the reference is changed
                        if (courselist != null)
                        {
                            w.write(Integer.toString(courselist.length));
                            w.newLine();
                            for (int i = 0; i < courselist.length; i++)
                            {
                                w.write(Data.gson.toJson(courselist[i]));
                                w.newLine();
                            }
                            //System.out.println("courselist saved");
                        }
                        else
                        {
                            w.write("null");
                            w.newLine();
                        }
                        boolean[] hasevents = Data.courses_hasevents;
                        if (hasevents != null)
                        {
                            w.write(Integer.toString(hasevents.length));
                            w.newLine();
                            for (int i = 0; i < hasevents.length; i++)
                            {
                                w.write(Boolean.toString(hasevents[i]));
                                w.newLine();
                            }
                        }
                        else
                        {
                            w.write("null");
                            w.newLine();
                        }

                    } catch (Exception ignored) {}
                }
            }
            catch (GeneralSecurityException | IOException ignored) {}
            if (msg.arg2 == RESTORE)
            {
                Data.data_restored = true;
                //System.out.println(Data.data_restored);
            }
            stopSelf(msg.arg1);
        }
    }
    @Override
    public void onCreate()
    {
        super.onCreate();
        instance = this;
        thread = new HandlerThread("Stud.IP cache service", Process.THREAD_PRIORITY_BACKGROUND);
        thread.start();
        h = new ServiceHandler(thread.getLooper());
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId)
    {
        //System.out.println("service called");
        Message m = h.obtainMessage();
        m.arg1 = startId;
        m.arg2 = intent.getIntExtra(RESTORE_KEY,SAVE);
        h.sendMessage(m);
        return Service.START_NOT_STICKY;
    }
    
    @Override
    public IBinder onBind(Intent intent)
    {
        // no need to bind, as the service is only used in this application and all data is accessible via Data
        return null;
    }
}