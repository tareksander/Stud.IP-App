package com.studip.api;

public abstract class RouteCallback implements Runnable
{
    private String s = null;
    private Exception error = null;
    public void setResult(String s)
    {
        this.s = s;
    }
    public void setError(Exception error)
    {
        this.error = error;
    }
    @Override
    public void run()
    {
        if (s != null)
        {
            routeFinished(s,null);
        }
        else
        {
            if (error != null)
            {
                routeFinished(null,error);
            }
        }
    }
    public abstract void routeFinished(String result, Exception error);
}
