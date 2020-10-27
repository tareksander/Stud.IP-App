package com.studip;

import android.app.AlertDialog;
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
import com.studip.api.Folder;
import com.studip.api.ManagedObjectListener;
import com.studip.api.News;
import com.studip.api.NewsList;
import com.studip.api.rest.StudipCourse;
import com.studip.api.rest.StudipListArray;
import com.studip.api.rest.StudipListObject;

import java.util.Arrays;

public class CoursesFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener
{
    CourseAdapter event_adapter;
    Handler h;
    private Callback listener = new Callback();
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState)
    {
        View v = inflater.inflate(R.layout.fragment_courses, container, false);
        SwipeRefreshLayout r = v.findViewById(R.id.event_refresh);
        r.setOnRefreshListener(this);
        
        // TODO
        /*
        to search for courses:
        dispatch.php/globalsearch/find?search=
         */
        
        
        ListView l = v.findViewById(R.id.event_list);
        event_adapter = new CourseAdapter(getActivity(), ArrayAdapter.NO_SELECTION);
        Data.coursesfragment = this;
        if (Data.courses == null)
        {
            h = HandlerCompat.createAsync(Looper.getMainLooper());
            
            // TODO add null checks, as the user data might be deformed or null, in case of a network disconnection
            Data.courses = new CourseList(Data.user.user_id,h);
        }
        Data.courses.addRefreshListener(listener);
        if (Data.courselist == null)
        {
            if (savedInstanceState != null)
            {
                try
                {
                    Data.courselist = Courses.ArrayFromList(Data.courses.getData());
                    event_adapter.notifyDataSetChanged();
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }
        l.setAdapter(event_adapter);
        
        return v;
    }

    private class Callback extends ManagedObjectListener<StudipListObject>
    {
        @Override
        public void callback(StudipListObject obj, Exception error)
        {
            try
            {
                //System.out.println("refreshed");
                Data.courselist = Courses.ArrayFromList(Data.courses.getData());
                event_adapter.notifyDataSetChanged();
                StudipCourse[] courses = Courses.ArrayFromList(Data.courses.getData());
                Data.courses_events_pending = new EventList[Data.courses.getData().pagination.total];
                if (Data.courses_hasevents != null)
                {
                    Data.courses_hasevents = Arrays.copyOf(Data.courses_hasevents, courses.length);
                }
                else
                {
                    Data.courses_hasevents = new boolean[courses.length];
                }
                for (int i = 0;i<Data.courses_events_pending.length;i++)
                {
                    //System.out.println(Data.api.submit(Data.api.new CourseRoute("events",courses[i].course_id)).get());
                    Data.courses_events_pending[i] = new EventList(h,courses[i].course_id);
                    Data.courses_events_pending[i].addRefreshListener(new HasEvents(i));
                    Data.courses_events_pending[i].refresh();
                }
            }
            catch (Exception e) {e.printStackTrace();}
            //System.out.println("main refresh done");
            //SwipeRefreshLayout r = getView().findViewById(R.id.event_refresh);
            //r.setRefreshing(false);
        }
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        if (Data.courselist == null)
        {
            if (savedInstanceState == null)
            {
                Data.courses.refresh();
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        Data.courses.removeRefreshListener(listener);
    }

    private class OnMembersClicked implements View.OnClickListener
    {
        String courseID;
        public OnMembersClicked(String courseID)
        {
            this.courseID = courseID;
        }
        private class DialogOnMemberClicked implements View.OnClickListener
        {
            @Override
            public void onClick(View v)
            {
                
            }
        }
        private class DialogAdapter extends ArrayAdapter<TextView>
        {
            public DialogAdapter(@NonNull Context context, int resource)
            {
                super(context, resource);
            }
            
        }
        
        @Override
        public void onClick(View v)
        {
            if (v.getVisibility() == View.VISIBLE)
            {
                View layout = getLayoutInflater().inflate(R.layout.dialog_course_members,null);
                DialogAdapter ad = new DialogAdapter(getActivity(), ArrayAdapter.NO_SELECTION);
                // TODO finish
                
                
                new AlertDialog.Builder(getActivity()).setTitle(R.string.members_dialog_title).setView(layout).show();
            }
        }
    }
    

    public class OnFilesClicked implements View.OnClickListener
    {
        String courseID;
        public OnFilesClicked(String courseID)
        {
            this.courseID = courseID;
        }
        @Override
        public void onClick(View v)
        {
            if (v.getVisibility() == View.VISIBLE)
            {
                if (Data.folder_provider == null)
                {
                    Data.folder_provider = new Folder(HandlerCompat.createAsync(Looper.getMainLooper()), Folder.TYPE_COURSE_TOP_FOLDER,courseID);
                    // refreshing is done after the creation of the FileFragment
                }
                else
                {
                    Data.folder_provider.reinit(Folder.TYPE_COURSE_TOP_FOLDER,courseID);
                    Data.folder_provider.refresh();
                }
                Data.home_activity.pager.setCurrentItem(2);
            }
        }
    }
    
    public class OnNewsClicked extends ManagedObjectListener<StudipListObject> implements View.OnClickListener, Runnable
    {
        private AlertDialog d;
        private NewsAdapter ad;
        private NewsList news;
        private String courseID;
        public OnNewsClicked(String courseID)
        {
            this.courseID = courseID;
        }
        @Override
        public void onClick(View v)
        {
            View layout = getLayoutInflater().inflate(R.layout.course_news_dialog,null);
            d = new AlertDialog.Builder(getActivity()).setView(layout).create();
            ad = new NewsAdapter(getActivity(),ArrayAdapter.NO_SELECTION);
            ListView newslist = layout.findViewById(R.id.course_news_list);
            newslist.setAdapter(ad);
            news = new NewsList(h,courseID);
            news.addRefreshListener(this);
            news.refresh();
            d.show();
        }

        @Override
        public void callback(StudipListObject obj, Exception error)
        {
            try
            {
                ad.news = News.ArrayFromList(news.getData());
                if (ad.news.length == 0)
                {
                    d.dismiss();
                }
                ad.notifyDataSetChanged();
            } catch (Exception e)
            {
                d.dismiss();
            }
        }
    }
    
    
    
    
    public class HasEvents extends ManagedObjectListener<StudipListArray>
    {
        int index;
        public HasEvents(int index)
        {
            this.index = index;
        }

        @Override
        public void callback(StudipListArray obj, Exception error)
        {
            //System.out.println("finished event route");
            try
            {
                StudipListArray l = Data.courses_events_pending[index].getData();
                if (l != null)
                {
                    if (l.pagination != null && l.pagination.total > 0)
                    {
                        Data.courses_hasevents[index] = true;
                        Data.coursesfragment.event_adapter.notifyDataSetChanged();
                    }
                    else
                    {
                        Data.courses_hasevents[index] = false;
                        Data.coursesfragment.event_adapter.notifyDataSetChanged();
                    }
                }
                else
                {
                    Data.courses_hasevents[index] = false;
                    Data.coursesfragment.event_adapter.notifyDataSetChanged();
                }
            } catch (Exception e)
            {
                Data.courses_hasevents[index] = false;
                Data.coursesfragment.event_adapter.notifyDataSetChanged();
            }
            synchronized (Data.pending_monitor)
            {
                //System.out.println("finished event route for index: "+index);
                if (Data.courses_events_pending[index] != null)
                {
                    Data.courses_events_pending[index].removeRefreshListener(this);
                    Data.courses_events_pending[index] = null;
                }
                for (int i = 0; i < Data.courses_events_pending.length; i++)
                {
                    if (Data.courses_events_pending[i] != null)
                        return;
                }
                //System.out.println("all pending requests done");

                View v = getView(); // getView returns null if the fragment is recreated when changing orientation and pending requests are running
                if (v != null)
                {
                    SwipeRefreshLayout r = getView().findViewById(R.id.event_refresh);
                    r.setRefreshing(false);
                    Data.coursesfragment.event_adapter.notifyDataSetChanged();
                }
            }
        }
    }
    
     
    private class CourseAdapter extends ArrayAdapter
    {
        public CourseAdapter(@NonNull Context context, int resource)
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
            t.setText(Data.courselist[position].title);
            ImageView img;
            if (Data.courselist[position].modules_object != null)
            {
                img = v.findViewById(R.id.course_forum);
                if (Data.courselist[position].modules_object.forum == null)
                {
                    img.setVisibility(View.INVISIBLE);
                }
                else
                {
                    img.setVisibility(View.VISIBLE);
                }
            }
            img = v.findViewById(R.id.course_news);
            if (false)
            {
                img.setVisibility(View.INVISIBLE);
            }
            else
            {
                img.setVisibility(View.VISIBLE);
            }
            img = v.findViewById(R.id.course_schedule);
            if (Data.courses_hasevents == null || (! Data.courses_hasevents[position]))
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
            else
            {
                img.setVisibility(View.VISIBLE);
            }
            img = v.findViewById(R.id.course_meetings);
            if (true)
            {
                img.setVisibility(View.INVISIBLE);
            }
            else
            {
                img.setVisibility(View.VISIBLE);
            }
            v.findViewById(R.id.course_members).setOnClickListener(new OnMembersClicked(Data.courselist[position].course_id));
            v.findViewById(R.id.course_files).setOnClickListener(new OnFilesClicked(Data.courselist[position].course_id));
            v.findViewById(R.id.course_news).setOnClickListener(new OnNewsClicked(Data.courselist[position].course_id));
            return v;
        }
        
        @Override
        public int getCount()
        {
            if (Data.courselist == null)
            {
                return 0;
            }
            return Data.courselist.length;
        }
    }
    
    
    
    
    @Override
    public void onRefresh()
    {
        Data.courses.refresh();
    }
}