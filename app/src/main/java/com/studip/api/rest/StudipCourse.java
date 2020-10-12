package com.studip.api.rest;
import com.google.gson.JsonObject;
public class StudipCourse
{
    public String course_id;
    public String number;
    public String title;
    public String subtitle;
    public String type;
    public String description;
    public String location;
    public JsonObject lecturers; // can't be easily represented in Java
    public Members members;
    public class Members
    {
        public String user;
        public int user_count;
        public String autor;
        public int autor_count;
        public String tutor;
        public int tutor_count;
        public String dozent;
        public int dozent_count;
    }
    public String start_semester;
    public String end_semester;
    public Modules modules;
    public class Modules
    {
        public String forum;
        public String documents;
        public String wiki;
    }
    public int group;
}
