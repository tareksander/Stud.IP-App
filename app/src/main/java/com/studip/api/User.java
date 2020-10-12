package com.studip.api;
import android.os.Handler;
import com.studip.Data;
import com.studip.api.rest.StudipUser;
public class User extends ManagedObject<StudipUser>
{
    private final String requestUserID;
    public User(Handler h)
    {
        super(StudipUser.class,h);
        requestUserID = null;
    }
    public User(String requestUserID,Handler h)
    {
        super(StudipUser.class,h);
        this.requestUserID = requestUserID;
    }
   
    public void refresh()
    {
        if (ref == null)
        {
            if (requestUserID == null)
            {
                ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new BasicRoute("user"),this));
            }
            else
            {
                ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new UserRoute("",requestUserID),this));
            }
        }
    }
}