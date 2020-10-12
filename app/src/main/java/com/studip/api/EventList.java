package com.studip.api;
import android.os.Handler;
import com.studip.Data;
import com.studip.api.rest.StudipListArray;
public class EventList extends ManagedObject<StudipListArray>
{
    public static int LIMIT_NONE = 10000;
    private String courseID;
    private int limit;
    public EventList(Handler h,String courseID,int limit)
    {
        super(StudipListArray.class, h);
        this.courseID = courseID;
        if (limit < 0)
        {
            this.limit = LIMIT_NONE;
        }
        else
        {
            this.limit = limit;
        }
    }
    public EventList(Handler h,String courseID)
    {
        super(StudipListArray.class, h);
        this.courseID = courseID;
        this.limit = LIMIT_NONE;
    }
    @Override
    public void refresh()
    {
        if (ref == null)
        {
            ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new CourseRoute("events?limit="+limit, courseID),this));
        }
    }
}
