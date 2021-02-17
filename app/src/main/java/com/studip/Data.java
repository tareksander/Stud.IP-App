package com.studip;
import android.widget.ArrayAdapter;
import com.google.gson.Gson;
import com.studip.api.API;
import com.studip.api.CourseList;
import com.studip.api.EventList;
import com.studip.api.Folder;
import com.studip.api.Messages;
import com.studip.api.NewsList;
import com.studip.api.User;
import com.studip.api.rest.StudipCourse;
import com.studip.api.rest.StudipFolder;
import com.studip.api.rest.StudipMessage;
import com.studip.api.rest.StudipUser;

import org.jsoup.Jsoup;
public class Data
{
    // global data for all Fragments and Activities
    public static API api;
    public static User user_provider;
    public static volatile StudipUser user;
    public static Settings settings;
    public static Gson gson;
    public static Jsoup jsoup; // TODO initialize in onCreate of HomeActivity if null, used to parse plugin pages
    
    public static HomeActivity home_activity;

    
    // set to true by the cache service after data has been restored, or if no data was found
    // all fields that are restored should be defined as volatile
    public static volatile boolean data_restored = false;
    
    
    // Data used by specific Fragments:
    // TODO reset all values to null on logout, as they are all user-specific
    // TODO clear the cache file on logout
    
    
    // HomeFragment:
    public static NewsList global_news;
    
    // CoursesFragment:
    public static CoursesFragment coursesfragment;
    public static CourseList courses;
    public static volatile boolean[] courses_hasevents;
    public static volatile StudipCourse[] courselist;
    public static final String pending_monitor = "";
    public static EventList[] courses_events_pending;
    public static ArrayAdapter courses_adapter;
    
    // FileFragment:
    public static FileFragment filefragment;
    public static StudipFolder current_folder;
    public static Folder folder_provider;
    
    
    // MessagesFragment:
    public static MessagesFragment messagesfragment;
    public static volatile StudipMessage[] messages;
    public static volatile StudipUser[] senders;
    public static Messages messages_provider;
    
    
    
    
}
