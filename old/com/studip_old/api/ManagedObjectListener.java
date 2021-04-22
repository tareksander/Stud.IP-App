package com.studip_old.api;
public abstract class ManagedObjectListener<T> implements Runnable
{
    private T obj;
    private Exception error;
    @Override
    public void run()
    {
        callback(obj,error);
    }
    void setResult(T obj)
    {
        this.obj = obj;
    }
    void setError(Exception e)
    {
        this.error = error;
    }
    public abstract void callback(T obj, Exception error);
}
