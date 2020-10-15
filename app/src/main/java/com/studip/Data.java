package com.studip;
import android.widget.ArrayAdapter;
import com.google.gson.Gson;
import com.studip.api.API;
import com.studip.api.CourseList;
import com.studip.api.EventList;
import com.studip.api.Folder;
import com.studip.api.User;
import com.studip.api.rest.StudipFolder;

import org.jsoup.Jsoup;
public class Data
{
    // global data for all Fragments and Activities
    public static API api;
    public static User user;
    public static Settings settings;
    public static Gson gson;
    public static Jsoup jsoup; // TODO initialize in onCreate of HomeActivity if null, used to parse plugin pages
    
    
    
    // Data used by specific Fragments:
    // TODO reset all values to null on logout, as they are all user-specific
    
    // CoursesFragment:
    public static CoursesFragment coursesfragment;
    public static CourseList courses;
    public static boolean[] courses_hasevents;
    public static final String pending_monitor = "";
    public static EventList[] courses_events_pending;
    public static ArrayAdapter courses_adapter;
    
    // FileFragment:
    public static FileFragment filefragment;
    public static StudipFolder current_folder;
    public static Folder folder_provider;
    
    
}
