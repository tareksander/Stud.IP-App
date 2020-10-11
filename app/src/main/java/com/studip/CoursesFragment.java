package com.studip;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.studip.api.ResponseParser;

import java.util.Iterator;
import java.util.Map;

public class CoursesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    EventAdapter event_adapter;
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_courses, container, false);
        SwipeRefreshLayout r = v.findViewById(R.id.event_refresh);
        r.setOnRefreshListener(this);
        ListView l = v.findViewById(R.id.event_list);
        event_adapter = new EventAdapter(getActivity(),ArrayAdapter.NO_SELECTION,new Course[0]);
        l.setAdapter(event_adapter);
        return v;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        updateCourses();
    }
    
    private void updateCourses()
    {
        try
        {
            String json = Data.api.getUserCourses().get();
            JsonObject root = ResponseParser.parseQuery(json);
            JsonObject collection = ResponseParser.getCollection(root);
            Course[] courses = new Course[collection.size()];
            Iterator<Map.Entry<String,JsonElement>> it =  collection.entrySet().iterator();
            for (int i = 0;i<courses.length;i++)
            {
                boolean forum = false, files = false, events = false;
                JsonObject course = it.next().getValue().getAsJsonObject();
                if (ResponseParser.getValue(course,"forum") != null)
                {
                    forum = true;
                }
                if (ResponseParser.getValue(course,"documents") != null)
                {
                    files = true;
                }
                String courseID = ResponseParser.getValue(course,"course_id");
                String events_json = Data.api.getCourseEvents(courseID).get();
                JsonObject events_root = ResponseParser.parseQuery(events_json);
                if (events_root != null)
                {
                    JsonObject events_collection = ResponseParser.getCollection(events_root);
                    if (events_collection.size() != 0)
                    {
                        events = true;
                    }
                }
                String name = ResponseParser.getValue(course,"title");
                courses[i] = new Course(courseID,name,forum,false,files,events,false,false,false,false,false,false,false,false);
            }
            event_adapter.courses = courses;
        } catch (Exception e) {e.printStackTrace();}
    }
    
    
    
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
    
    private class EventAdapter extends ArrayAdapter
    {
        Course[] courses;
        public EventAdapter(@NonNull Context context, int resource,Course[] events)
        {
            super(context, resource);
            this.courses = events;
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
            t.setText(courses[position].name);
            if (! courses[position].forum)
            {
                ImageView img = v.findViewById(R.id.course_forum);
                img.setVisibility(View.INVISIBLE);
            }
            if (! courses[position].news)
            {
                ImageView img = v.findViewById(R.id.course_news);
                img.setVisibility(View.INVISIBLE);
            }
            if (! courses[position].schedule)
            {
                ImageView img = v.findViewById(R.id.course_schedule);
                img.setVisibility(View.INVISIBLE);
            }
            if (! courses[position].courseware)
            {
                ImageView img = v.findViewById(R.id.course_courseware);
                img.setVisibility(View.INVISIBLE);
            }
            if (! courses[position].meetings)
            {
                ImageView img = v.findViewById(R.id.course_meetings);
                img.setVisibility(View.INVISIBLE);
            }
            return v;
        }
        
        @Override
        public int getCount()
        {
            return courses.length;
        }
    }
    
    
    
    
    @Override
    public void onRefresh()
    {
        updateCourses();
        SwipeRefreshLayout r = getView().findViewById(R.id.event_refresh);
        r.setRefreshing(false);
    }
}