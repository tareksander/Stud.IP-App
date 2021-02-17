package com.studip.api;
import android.os.Handler;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.studip.Data;
import com.studip.api.rest.StudipForumCategory;
import com.studip.api.rest.StudipForumEntry;
import com.studip.api.rest.StudipListObject;
import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Map;
public class Forum extends RouteCallback
{
    private final ArrayDeque<String> parent = new ArrayDeque<>();
    private final ForumCallback listener;
    private final Handler h;
    public Forum(String courseID,ForumCallback listener, Handler h)
    {
        this.listener = listener;
        this.h = h;
        parent.add(API.API+"course/"+courseID+"/forum_categories");
    }
    public void setCourse(String courseID)
    {
        synchronized (parent)
        {
            parent.clear();
            parent.add(API.API + "course/" + courseID + "/forum_categories?limit=1000");
        }
    }
    public void refresh()
    {
        synchronized (parent)
        {
            Data.api.submitWithCallback(Data.api.new CallbackRoute(Data.api.new BlankRoute(parent.peek()),this));
        }
    }
    public void goDownCategory(String categoryID)
    {
        synchronized (parent)
        {
            parent.push(API.API + "forum_category/" + categoryID + "/areas?limit=1000");
        }
    }
    public void goDownEntry(String entryID)
    {
        synchronized (parent)
        {
            parent.push(API.API + "forum_entry/" + entryID);
        }
    }
    public void goUp()
    {
        if (parent.size() > 1)
        {
            parent.pop();
        }
    }
    public int getDepth()
    {
        return parent.size();
    }
    @Override
    public void routeFinished(String result, Exception error)
    {
        if (result != null)
        {
            try
            {
                StudipListObject l = Data.gson.fromJson(result, StudipListObject.class);
                if (getDepth() == 1)
                {
                    Iterator<Map.Entry<String, JsonElement>> it = l.collection.entrySet().iterator();
                    StudipForumCategory[] entries = new StudipForumCategory[l.pagination.total];
                    int index = 0;
                    while (it.hasNext())
                    {
                        JsonElement e = it.next().getValue();
                        entries[index] = Data.gson.fromJson(e, StudipForumCategory.class);
                        index++;
                    }
                    listener.setObject(entries);
                    h.post(listener);
                    return;
                }
                if (getDepth() == 2)
                {
                    Iterator<Map.Entry<String, JsonElement>> it = l.collection.entrySet().iterator();
                    StudipForumEntry[] entries = new StudipForumEntry[l.pagination.total];
                    int index = 0;
                    while (it.hasNext())
                    {
                        JsonElement e = it.next().getValue();
                        entries[index] = Data.gson.fromJson(e, StudipForumEntry.class);
                        index++;
                    }
                    listener.setObject(entries);
                    h.post(listener);
                    return;
                }
                if (getDepth() > 2)
                {
                    StudipForumEntry e = Data.gson.fromJson(result, StudipForumEntry.class);
                    listener.setObject(e);
                    h.post(listener);
                    return;
                }
            } catch (JsonSyntaxException e)
            {
                e.printStackTrace();
            }
        }
        if (error != null)
        {
            listener.setObject(error);
            h.post(listener);
        }
    }
    public static abstract class ForumCallback implements Runnable
    {
        private Object o;
        public void setObject(Object o)
        {
            this.o = o;
        }
        @Override
        public void run()
        {
            forumRefreshed(o);
        }
        public abstract void forumRefreshed(Object o); // returns an array of either StudipForumCategory or StudipForumEntry, a single StudipForumEntry or a single Exception object
    }
}
