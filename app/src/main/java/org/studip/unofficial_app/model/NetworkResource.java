package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import org.jetbrains.annotations.NotNull;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// cannot be generic for the type, so has to use raw types
@SuppressWarnings("rawtypes")
public abstract class NetworkResource<T>
{
    protected abstract LiveData<T> getDBData(Context c);
    protected abstract Call getCall(Context c);
    protected abstract void updateDB(Context c,Object res);
    
    protected final MutableLiveData<Integer> status = new MutableLiveData<>(-1);
    protected final MutableLiveData<Boolean> refreshing = new MutableLiveData<>(false);
    private LiveData<T> res = null;
    
    private Context app;
    public NetworkResource(Context c) {
        app = c.getApplicationContext();
    }
    
    public LiveData<T> get() {
        if (res == null) {
            res = Transformations.distinctUntilChanged(getDBData(app));
            app = null;
        }
        return res;
    }
    
    public LiveData<Integer> getStatus() {
        return status;
    }
    public LiveData<Boolean> isRefreshing() {
        return refreshing;
    }
    
    // cannot be generic for the type, so has to use raw types
    @SuppressWarnings("unchecked")
    public void refresh(Context con) {
        Context c = con.getApplicationContext();
        if (refreshing.getValue() != null && ! refreshing.getValue()) {
            refreshing.setValue(true);
            getCall(con).enqueue(new Callback()
            {
                @Override
                public void onResponse(@NotNull Call call, @NotNull Response response)
                {
                    Object res = response.body();
                    status.setValue(response.code());
                    if (res != null) {
                        new Thread(() -> {
                            try {
                                updateDB(c,res);
                            } catch (Exception ignored) {
                                //ignored.printStackTrace();
                            }
                            refreshing.postValue(false);
                        }).start();
                    } else {
                        //System.out.println("no response");
                        refreshing.setValue(false);
                    }
                }
                @Override
                public void onFailure(@NotNull Call call, @NotNull Throwable t)
                {
                    //System.out.println("network call failed");
                    t.printStackTrace();
                    refreshing.setValue(false);
                }
            });
        }
    }
    private static final Pattern pathPattern = Pattern.compile(".*/([^/]+)$");
    public static String lastPathSegment(String path) {
        if (path != null) {
            //System.out.println("Path: "+path);
            Matcher m = pathPattern.matcher(path);
            if (m.matches())
            {
                String part = m.group(1);
                if (part != null)
                {
                    path = part;
                    //System.out.println("segment: " + path);
                }
            }
        }
        return path;
    }
}
