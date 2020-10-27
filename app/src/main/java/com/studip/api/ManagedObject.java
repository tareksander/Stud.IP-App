package com.studip.api;
import android.os.Handler;
import com.studip.Data;
import java.util.ArrayList;
import java.util.concurrent.Future;
public abstract class ManagedObject<T> extends RouteCallback
{
    Future ref;
    private ArrayList<ManagedObjectListener<T>> listeners = new ArrayList<>();
    T obj;
    private final Class<T> c;
    private final Handler h;
    public ManagedObject(Class<T> c,Handler h)
    {
        this.c = c;
        this.h = h;
    }
    public void addRefreshListener(ManagedObjectListener<T> listener)
    {
        listeners.add(listener);
    }
    public void removeRefreshListener(ManagedObjectListener<T> listener)
    {
        listeners.remove(listener);
    }
    @Override
    public void routeFinished(String result, Exception error)
    {
        if (result != null)
        {
            try
            {
                String json = result;
                obj = (T) Data.gson.fromJson(json,c);
            } catch (Exception e)
            {
                e.printStackTrace();
                for (ManagedObjectListener<T> listener : listeners)
                {
                    listener.setResult(null);
                    listener.setError(e);
                    h.post(listener);
                }
                return;
            }
            //System.out.println("route finished, calling listeners");
            ref = null;
            for (ManagedObjectListener<T> listener : listeners)
            {
                listener.setResult(obj);
                listener.setError(null);
                h.post(listener);
            }
            return;
        }
        refresh();
        //error.printStackTrace();
    }
    public abstract void refresh();
    public boolean refreshed()
    {
        return ref == null;
    }

    // may return null if the first request after creation failed
    public T getData()
    {
        //process_pending_refresh();
        if (obj == null)
        {
            if (ref == null)
            {
                refresh();
            }
            try
            {
                ref.get();
            }
            catch (Exception ignored) {}
        }
        return obj;
    }
}
