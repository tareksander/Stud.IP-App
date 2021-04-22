package com.studip_old.api;
import android.os.Handler;
import com.studip_old.Data;
import com.studip_old.api.rest.StudipListObject;
public class NewsList extends ManagedObject<StudipListObject>
{
    private final String courseID;
    public NewsList(Handler h, String courseID)
    {
        super(StudipListObject.class, h);
        this.courseID = courseID;
    }
    @Override
    public void refresh()
    {
        if (ref == null)
        {
            if (courseID == null || courseID.equals(""))
            {
                ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new BlankRoute(API.API+"studip/news?limit=1000"),this));
            }
            else
            {
                ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new CourseRoute("news?limit=1000",courseID),this));
            }
        }
    }
}
