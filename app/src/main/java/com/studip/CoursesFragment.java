package com.studip;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.core.os.HandlerCompat;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.studip.api.CourseList;
import com.studip.api.Courses;
import com.studip.api.EventList;
import com.studip.api.RouteCallback;
import com.studip.api.rest.StudipCourse;
import com.studip.api.rest.StudipListArray;
import com.studip.api.rest.StudipListObject;

import java.util.Arrays;

public class CoursesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Runnable
{
    EventAdapter event_adapter;
    CourseList l;
    private final String pending_monitor = "";
    EventList[] pending;
    Handler h;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_courses, container, false);
        SwipeRefreshLayout r = v.findViewById(R.id.event_refresh);
        r.setOnRefreshListener(this);
        
        // TODO add null checks here, as the object might me malformed or null
        h = HandlerCompat.createAsync(Looper.getMainLooper());
        this.l = new CourseList(Data.user.getData().user_id,h);
        this.l.addRefreshListener(this);
        
        
        
        ListView l = v.findViewById(R.id.event_list);
        
        event_adapter = new EventAdapter(getActivity(),ArrayAdapter.NO_SELECTION);
        l.setAdapter(event_adapter);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        l.refresh();
    }
    

    @Override
    public void run()
    {
        try
        {
            if (event_adapter.events != null)
            {
                event_adapter.events = Arrays.copyOf(event_adapter.events,l.getData().pagination.total);
            }
            else
            {
                event_adapter.events = new boolean[l.getData().pagination.total];
            }
            event_adapter.courselist = Courses.ArrayFromList(l.getData());
            event_adapter.notifyDataSetChanged();
            StudipCourse[] courses = Courses.ArrayFromList(l.getData());
            pending = new EventList[l.getData().pagination.total];
            for (int i = 0;i<pending.length;i++)
            {
                //System.out.println(Data.api.submit(Data.api.new CourseRoute("events",courses[i].course_id)).get());
                pending[i] = new EventList(h,courses[i].course_id);
                pending[i].addRefreshListener(new HasEvents(i));
                pending[i].refresh();
            }
        }
        catch (Exception e) {e.printStackTrace();}
        //System.out.println("main refresh done");
        //SwipeRefreshLayout r = getView().findViewById(R.id.event_refresh);
        //r.setRefreshing(false);
    }
    
    
    public class HasEvents implements Runnable
    {
        int index;
        public HasEvents(int index)
        {
            this.index = index;
        }
        @Override
        public void run()
        {
            //System.out.println("finished event route");
            try
            {
                StudipListArray l = pending[index].getData();
                if (l != null)
                {
                    if (l.pagination != null && l.pagination.total > 0)
                    {
                        event_adapter.events[index] = true;
                        event_adapter.notifyDataSetChanged();
                    }
                    else 
                    {
                        event_adapter.events[index] = false;
                        event_adapter.notifyDataSetChanged();
                    }
                }
                else
                {
                    event_adapter.events[index] = false;
                    event_adapter.notifyDataSetChanged();
                }
            } catch (Exception e)
            {
                event_adapter.events[index] = false;
                event_adapter.notifyDataSetChanged();
            }
            synchronized (pending_monitor)
            {
                //System.out.println("finished event route for index: "+index);
                pending[index].removeRefreshListener(this);
                pending[index] = null;
                for (int i = 0; i < pending.length; i++)
                {
                    if (pending[i] != null)
                        return;
                }
                //System.out.println("all pending requests done");
                SwipeRefreshLayout r = getView().findViewById(R.id.event_refresh);
                r.setRefreshing(false);
                event_adapter.notifyDataSetChanged();
            }
        }
    }
    
    /*
    private class Course
    {
        String courseID;
        String name;
        boolean forum;
        boolean news;
        boolean files;
        boolean schedule;
        boolean courseware;
        boolean meetings;
        boolean forum_new;
        boolean news_new;
        boolean files_new;
        boolean schedule_new;
        boolean courseware_new;
        boolean meetings_new;
        public Course(String courseID,String name,boolean forum,boolean news,boolean files,boolean schedule,boolean courseware,boolean meetings
                                               ,boolean forum_new,boolean news_new,boolean files_new,boolean schedule_new,boolean courseware_new,boolean meetings_new)
        {
            this.courseID = courseID;
            this.name = name;
            this.forum = forum;
            this.news = news;
            this.files = files;
            this.schedule = schedule;
            this.courseware = courseware;
            this.meetings = meetings;
            this.forum_new = forum_new;
            this.news_new = news_new;
            this.files_new = files_new;
            this.schedule_new = schedule_new;
            this.courseware_new = courseware_new;
            this.meetings_new = meetings_new;
        }
    }
    */
    
     
    private class EventAdapter extends ArrayAdapter
    {
        StudipCourse[] courselist;
        boolean[] events;
        public EventAdapter(@NonNull Context context, int resource)
        {
            super(context, resource);
        }
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
        {
            View v;
            if (convertView != null)
            {
                v = convertView;
            }
            else
            {
                v = getLayoutInflater().inflate(R.layout.courses_entry,parent,false);
            }
            TextView t = v.findViewById(R.id.course_name);
            t.setText(courselist[position].title);
            ImageView img;
            img = v.findViewById(R.id.course_forum);
            if (courselist[position].modules == null || (courselist[position].modules.forum == null))
            {
                img.setVisibility(View.INVISIBLE);
            }
            else
            {
                img.setVisibility(View.VISIBLE);
            }
            img = v.findViewById(R.id.course_news);
            if (true)
            {
                img.setVisibility(View.INVISIBLE);
            }
            img = v.findViewById(R.id.course_schedule);
            if (! events[position])
            {
                img.setVisibility(View.INVISIBLE);
            }
            else
            {
                img.setVisibility(View.VISIBLE);
            }
            img = v.findViewById(R.id.course_courseware);
            if (true)
            {
                img.setVisibility(View.INVISIBLE);
            }
            img = v.findViewById(R.id.course_meetings);
            if (true)
            {
                img.setVisibility(View.INVISIBLE);
            }
            return v;
        }
        
        @Override
        public int getCount()
        {
            if (courselist == null)
            {
                return 0;
            }
            return courselist.length;
        }
    }
    
    
    
    
    @Override
    public void onRefresh()
    {
        l.refresh();
    }
}