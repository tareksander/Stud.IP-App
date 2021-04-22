package com.studip_old.api;

public abstract class ByteRouteCallback implements Runnable
{
    private byte[] b = null;
    private Exception error = null;
    public void setResult(byte[] b)
    {
        this.b = b;
    }
    public void setError(Exception error)
    {
        this.error = error;
    }
    @Override
    public void run()
    {
        if (b != null)
        {
            routeFinished(b,null);
        }
        else
        {
            if (error != null)
            {
                routeFinished(null,error);
            }
        }
    }
    public abstract void routeFinished(byte[] result, Exception error);
}
