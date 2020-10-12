package com.studip.api.rest;
import com.google.gson.JsonObject;
public class StudipList
{
    public JsonObject collection; // the collection itself can't easily be represented in Java
    public Pagination pagination;
    public class Pagination
    {
        public int total;
        public int offset;
        public int limit;
    }
}
