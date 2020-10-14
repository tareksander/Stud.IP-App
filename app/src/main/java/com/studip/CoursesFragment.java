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

import com.google.gson.JsonObject;
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
    private static final String courselist_key = "coursefragment_courselist";
    private static final String courselist_lecturers_key = "coursefragment_courselist_lecturers";
    private static final String eventlist_key = "coursefragment_eventlist";
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

        event_adapter = new EventAdapter(getActivity(), ArrayAdapter.NO_SELECTION);
        if (savedInstanceState != null)
        {
            //System.out.println("restoring courses state");
            event_adapter.events = (boolean[]) savedInstanceState.getSerializable(eventlist_key);
            event_adapter.courselist = (StudipCourse[]) savedInstanceState.getSerializable(courselist_key);
            for (int i = 0;i<event_adapter.courselist.length;i++)
            {
                event_adapter.courselist[i].lecturers = Data.gson.fromJson(savedInstanceState.getString(courselist_lecturers_key+i), JsonObject.class);
            }
        }
        l.setAdapter(event_adapter);
        
        return v;
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState)
    {
        super.onSaveInstanceState(outState);
        //System.out.println("saving courses state");
        outState.putSerializable(courselist_key,event_adapter.courselist);
        for (int i = 0;i<event_adapter.courselist.length;i++)
        {
            outState.putString(courselist_lecturers_key+i,Data.gson.toJson(event_adapter.courselist[i].lecturers));
        }
        outState.putSerializable(eventlist_key,event_adapter.events);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (savedInstanceState == null)
        {
            //System.out.println("getting courses information");
            l.refresh();
        }
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