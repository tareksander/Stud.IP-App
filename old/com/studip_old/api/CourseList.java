package com.studip_old.api;
import android.os.Handler;
import com.studip_old.Data;
import com.studip_old.api.rest.StudipListObject;
public class CourseList extends ManagedObject<StudipListObject>
{
    private final String userID;
    private String semesterID;
    public CourseList(String userID, Handler h)
    {
        super(StudipListObject.class,h);
        this.userID = userID;
    }
    public CourseList(String userID,String semesterID, Handler h)
    {
        this(userID,h);
        this.semesterID = semesterID;
    }
    @Override
    public void refresh()
    {
        if (ref == null)
        {
            if (userID == null)
            {
                throw new NullPointerException(); // TODO for now just throw, but if no user id is supplied it should return all courses in the system
            }
            else
            {
                if (semesterID == null)
                {
                    ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new UserRoute("courses?limit=100000", userID),this));
                }
                else
                {
                    ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new UserRoute("courses?limit=100000&"+semesterID, userID),this));
                }
            }
        }
    }
}