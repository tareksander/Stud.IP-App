package com.studip.api;
import android.os.Handler;
import com.studip.Data;
import com.studip.api.rest.StudipListObject;
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
            ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new UserRoute("inbox",Data.user.user_id),this));
        }
    }
}
