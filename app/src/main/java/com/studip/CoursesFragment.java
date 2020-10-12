package com.studip;

import android.content.Context;
import android.os.Bundle;
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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.studip.api.CourseList;
import com.studip.api.Courses;
import com.studip.api.ResponseParser;
import com.studip.api.rest.StudipCourse;
import com.studip.api.rest.StudipList;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class CoursesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener, Runnable
{
    EventAdapter event_adapter;
    CourseList l;
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
        this.l = new CourseList(Data.user.getData().user_id, HandlerCompat.createAsync(Looper.getMainLooper()));
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
            event_adapter.courselist = Courses.ArrayFromList(l.getData());
            event_adapter.notifyDataSetChanged();
            SwipeRefreshLayout r = getView().findViewById(R.id.event_refresh);
            r.setRefreshing(false);
        }
        catch (Exception e) {}
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
            if (courselist[position].modules == null || (courselist[position].modules.forum == null))
            {
                ImageView img = v.findViewById(R.id.course_forum);
                img.setVisibility(View.INVISIBLE);
            }
            if (true)
            {
                ImageView img = v.findViewById(R.id.course_news);
                img.setVisibility(View.INVISIBLE);
            }
            if (true)
            {
                // TODO the events have to be fetched course-by-course, but you can use ?limit=0 to just get the pagination, because for this menu we only want to know if there are any
                ImageView img = v.findViewById(R.id.course_schedule);
                img.setVisibility(View.INVISIBLE);
            }
            if (true)
            {
                ImageView img = v.findViewById(R.id.course_courseware);
                img.setVisibility(View.INVISIBLE);
            }
            if (true)
            {
                ImageView img = v.findViewById(R.id.course_meetings);
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