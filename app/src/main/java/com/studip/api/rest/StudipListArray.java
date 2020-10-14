package com.studip.api.rest;
import com.google.gson.JsonArray;
import java.io.Serializable;
public class StudipListArray
{
    public JsonArray collection; // the collection itself can't easily be represented in Java
    // for the course list of a user collection is a JsonObject, for the event list of a course it is a JsonArray ?!?
    public Pagination pagination;
    public class Pagination  implements Serializable
    {
        public int total;
        public int offset;
        public int limit;
    }
}
