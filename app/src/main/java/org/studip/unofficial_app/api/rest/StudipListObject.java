package org.studip.unofficial_app.api.rest;

import com.google.gson.JsonObject;

import java.io.Serializable;

public class StudipListObject implements Serializable
{
    // because the Gson instance is created to include transient fields, you just have to serialize
    // it manually with Gson when using java serialization
    public transient JsonObject collection; // the collection itself can't easily be represented in Java
    // for the course list of a user collection is a JsonObject, for the event list of a course it is a JsonArray ?!?
    public Pagination pagination;
    public static class Pagination implements Serializable
    {
        public int total;
        public int offset;
        public int limit;
    }
}
