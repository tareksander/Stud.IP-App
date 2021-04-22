package com.studip_old.api;
import android.os.Handler;

import com.studip_old.Data;
import com.studip_old.api.rest.StudipFolder;
public class Folder extends ManagedObject<StudipFolder>
{
    public static final int TYPE_USER_TOP_FOLDER = 1;
    public static final int TYPE_COURSE_TOP_FOLDER = 2;
    public static final int TYPE_FOLDER = 3;
    private int type;
    private String id;
    public Folder(Handler h,int type,String id)
    {
        super(StudipFolder.class, h);
        this.type = type;
        this.id = id;
    }
    public void reinit(int type,String id)
    {
        this.type = type;
        this.id = id;
        if (ref != null)
        {
            ref.cancel(true); // cancel a running refresh
        }
        ref = null;
        obj = null;
    }
    public String getId()
    {
        return id;
    }
    public int getType()
    {
        return type;
    }
    @Override
    public void refresh()
    {
        if (ref == null)
        {
            switch (type)
            {
                case TYPE_USER_TOP_FOLDER:
                    ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new UserRoute("top_folder",id),this));
                    return;
                case TYPE_COURSE_TOP_FOLDER:
                    ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new CourseRoute("top_folder",id),this));
                    return;
                case TYPE_FOLDER:
                    ref = Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new FolderRoute("",id),this));
                    return;
                default:
                    throw new RuntimeException("invalid type For com.studip.api.Folder");
            }
        }
    }
}
