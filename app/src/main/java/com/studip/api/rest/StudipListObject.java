package com.studip.api.rest;
import com.google.gson.JsonObject;
public class StudipListObject
{
    public JsonObject collection; // the collection itself can't easily be represented in Java
    // for the course list of a user collection is a JsonObject, for the event list of a course it is a JsonArray ?!?
    public Pagination pagination;
    public class Pagination
    {
        public int total;
        public int offset;
        public int limit;
    }
}
