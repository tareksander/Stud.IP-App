package com.studip_old.api;
import android.os.Handler;
import com.studip_old.Data;
import com.studip_old.api.rest.StudipListObject;
public class Messages extends ManagedObject<StudipListObject>
{
    private int offset,limit;
    public Messages(Handler h)
    {
        super(StudipListObject.class, h);
        offset = 0;
        limit = 1000000;
    }
    public Messages(Handler h,int offset, int limit)
    {
        super(StudipListObject.class, h);
        this.offset = offset;
        this.limit = limit;
    }
    public void reinit(int offset, int limit)
    {
        this.offset = offset;
        this.limit = limit;
    }
    @Override
    public void refresh()
    {
        if (ref == null)
        {
            ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new UserRoute("inbox?limit="+limit,Data.user.user_id),this));
        }
    }
}
