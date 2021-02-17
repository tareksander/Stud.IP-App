package com.studip;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.util.Linkify;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.core.os.HandlerCompat;
import androidx.core.widget.NestedScrollView;
import androidx.fragment.app.Fragment;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.studip.api.API;
import com.studip.api.CourseList;
import com.studip.api.Courses;
import com.studip.api.EventList;
import com.studip.api.Folder;
import com.studip.api.Forum;
import com.studip.api.ManagedObjectListener;
import com.studip.api.News;
import com.studip.api.NewsList;
import com.studip.api.rest.StudipCourse;
import com.studip.api.rest.StudipForumCategory;
import com.studip.api.rest.StudipForumEntry;
import com.studip.api.rest.StudipListArray;
import com.studip.api.rest.StudipListObject;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
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
        CoursesFragment old = Data.coursesfragment;
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
    
    private class OnForumClicked extends Forum.ForumCallback implements View.OnClickListener
    {
        private final String courseID;
        public AlertDialog dialog;
        private Forum f;
        private ForumAdapter ad;
        private StudipForumEntry entry = null;
        private EditText subject;
        private EditText content;
        public OnForumClicked(String courseID)
        {
            this.courseID = courseID;
        }
        @Override
        public void onClick(View v)
        {
            f = new Forum(courseID,this,h);
            View layout = getLayoutInflater().inflate(R.layout.dialog_forum,null,false);
            dialog = new AlertDialog.Builder(getActivity()).setView(layout).create();
            ad = new ForumAdapter(getActivity(),ArrayAdapter.NO_SELECTION);
            Button submit = layout.findViewById(R.id.forum_submit);
            submit.setOnClickListener(this::submitEntry);
            subject = layout.findViewById(R.id.forum_subject);
            content = layout.findViewById(R.id.forum_content);
            ListView l = layout.findViewById(R.id.forum_list);
            l.setAdapter(ad);
            f.refresh();
            dialog.setOnKeyListener((dialog, keyCode, event) ->
            {
                if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_UP)
                {
                    //System.out.println("back pressed. depth: "+f.getDepth());
                    if (f.getDepth() <= 1)
                    {
                        dialog.dismiss();
                        return true;
                    }
                    f.goUp();
                    f.refresh();
                    return true;
                }
                return false;
            });
            
            
            dialog.show();
        }
        void submitEntry(View v) {
            if (entry != null)
            {
                String post_data = null;
                try
                {
                    if ("1".equals(entry.depth))
                    {
                        System.out.println("URL: "+"/forum_entry/"+entry.topic_id);
                        post_data = "subject="+ URLEncoder.encode(subject.getText().toString(),"UTF-8")+"&content="+URLEncoder.encode(content.getText().toString(),"UTF-8");
                        System.out.println("Data: "+post_data);
                        Data.api.submit(Data.api.new BasicRoute("/forum_entry/"+entry.topic_id, API.METHOD_POST,post_data)).get();
                        f.refresh();
                    }
                    if ("2".equals(entry.depth)) {
                        System.out.println("URL: "+"/forum_entry/"+entry.topic_id);
                        post_data = "content="+URLEncoder.encode(content.getText().toString(),"UTF-8");
                        System.out.println("Data: "+post_data);
                        Data.api.submit(Data.api.new BasicRoute("/forum_entry/"+entry.topic_id, API.METHOD_POST,post_data)).get();
                        f.refresh();
                    }
                }  catch (Exception e)
                {
                    e.printStackTrace();
                    dialog.dismiss();
                }
            }
        }
        private class ForumAdapter extends ArrayAdapter
        {
            Object o;
            public ForumAdapter(@NonNull Context context, int resource)
            {
                super(context, resource);
            }
            @SuppressLint("SetTextI18n")
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent)
            {
                TextView v;
                final int finalP = position;
                if (convertView == null)
                {
                    v = new TextView(getContext());
                    v.setAutoLinkMask(Linkify.WEB_URLS | Linkify.EMAIL_ADDRESSES);
                }
                else
                {
                    v = (TextView) convertView;
                    v.setOnClickListener(null);
                }
                if (o != null)
                {
                    if (o instanceof StudipForumCategory[])
                    {
                        //System.out.println("category");
                        StudipForumCategory[] c = (StudipForumCategory[]) o;
                        v.setText(c[position].entry_name);
                        v.setOnClickListener(v1 ->
                        {
                            f.goDownCategory(c[finalP].category_id);
                            f.refresh();
                        });
                    }
                    if (o instanceof StudipForumEntry[])
                    {
                        //System.out.println("areas");
                        StudipForumEntry[] e = (StudipForumEntry[]) o;
                        v.setText(e[position].subject);
                        v.setOnClickListener(v1 ->
                        {
                            f.goDownEntry(e[finalP].topic_id);
                            f.refresh();
                        });
                    }
                    if (o instanceof  StudipForumEntry)
                    {
                        //System.out.println("entry");
                        StudipForumEntry e = (StudipForumEntry) o;
                        StudipForumEntry[] children = e.children;
                        //System.out.println(e.depth);
                        if ("1".equals(e.depth))
                        {
                            final int p2 = children.length-1-position;
                            Document d = Jsoup.parse(children[children.length-1-position].subject); // the newest element is at the bottom, but we don't want that for the post list
                            v.setText(d.wholeText());
                            v.setOnClickListener(v12 ->
                            {
                                f.goDownEntry(e.children[p2].topic_id);
                                f.refresh();
                            });
                            return v;
                        }
                        if ("2".equals(e.depth))
                        {
                            //System.out.println("depth 2");
                            if (position == 0)
                            {
                                Document d = Jsoup.parse(e.subject);
                                Document c = Jsoup.parse(e.content);
                                v.setText(d.wholeText()+"\n\n"+c.wholeText());
                            }
                            else
                            {
                                position--;
                                Document c = Jsoup.parse(children[position].content);
                                v.setText(c.wholeText());
                            }
                            return v;
                        }
                        Document d = Jsoup.parse(e.content);
                        v.setText(d.wholeText());
                        return  v;
                    }
                }
                return v;
            }

            @Override
            public int getCount()
            {
                if (o == null)
                {
                    return 0;
                }
                if (o instanceof StudipForumEntry)
                {
                    StudipForumEntry e = (StudipForumEntry) o;
                    if (e.children == null)
                    {
                        return 0;
                    }
                    else
                    {
                        if ("1".equals(e.depth))
                        {
                            return e.children.length;
                        }
                        else
                        {
                            return e.children.length + 1; // we also need to display the entry itself
                        }
                    }
                }
                if (o instanceof StudipForumEntry[])
                {
                    return ((StudipForumEntry[])o).length;
                }
                if (o instanceof StudipForumCategory[])
                {
                    return ((StudipForumCategory[])o).length;
                }
                return 0;
            }
        }
        @Override
        public void forumRefreshed(Object o)
        {
            //System.out.println("forum refreshed");
            if (o instanceof Exception)
            {
                Exception e = (Exception) o;
                e.printStackTrace();
                return;
            }
            View subject = dialog.findViewById(R.id.forum_subject);
            View content = dialog.findViewById(R.id.forum_content);
            View submit = dialog.findViewById(R.id.forum_submit);
            if (o instanceof StudipForumEntry)
            {
                //System.out.println("entry");
                StudipForumEntry e = (StudipForumEntry) o;
                entry = e;
                if ("1".equals(e.depth))
                {
                    subject.setVisibility(View.VISIBLE);
                    content.setVisibility(View.VISIBLE);
                    submit.setVisibility(View.VISIBLE);
                }
                else
                {
                    if ("2".equals(e.depth))
                    {
                        subject.setVisibility(View.GONE);
                        content.setVisibility(View.VISIBLE);
                        submit.setVisibility(View.VISIBLE);
                    }
                    else
                    {
                        subject.setVisibility(View.GONE);
                        content.setVisibility(View.GONE);
                        submit.setVisibility(View.GONE);
                    }
                }
            }
            else
            {
                subject.setVisibility(View.GONE);
                content.setVisibility(View.GONE);
                submit.setVisibility(View.GONE);
            }
            ad.o = o;
            ad.notifyDataSetChanged();
        }
    }
    
    
    private class OnMembersClicked implements View.OnClickListener
    {
        private final String courseID;
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
        private final String courseID;
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
        private final String courseID;
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

                View v = Data.coursesfragment.getView();
                if (v != null)
                {
                    SwipeRefreshLayout r = v.findViewById(R.id.event_refresh);
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
            v.findViewById(R.id.course_forum).setOnClickListener(new OnForumClicked(Data.courselist[position].course_id));
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