package com.studip_old.api.rest;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
public class StudipCourse implements Serializable
{
    public String course_id;
    public String number;
    public String title;
    public String subtitle;
    public String type;
    public String description;
    public String location;
    // because the Gson instance is created to include transient fields, you just have to serialize
    // it manually with Gson when using java serialization
    public transient JsonObject lecturers; // can't be easily represented in Java
    public Members members;
    public static class Members  implements Serializable
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
    public transient JsonElement modules; // if empty, it's an empty array, but if there are elements, it's an object !?!
    @Expose(serialize = false, deserialize = false) // this shouldn't get serialized by Gson, modules already is
    public transient Modules modules_object;
    public static class Modules  implements Serializable
    {
        public String forum;
        public String documents;
        public String wiki;
    }
    public int group;
    
    
    
}
