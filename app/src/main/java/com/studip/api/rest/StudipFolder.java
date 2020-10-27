package com.studip.api.rest;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

public class StudipFolder
{
    public String id;
    public String user_id;
    public String parent_id;
    public String range_id;
    public String range_type;
    public String folder_type;
    public String name;
    public transient JsonElement data_content;
    public String description;
    public int mkdate;
    public int chdate;
    public boolean is_visible;
    public boolean is_readable;
    public boolean is_writable;
    public SubFolder[] subfolders;
    public static class SubFolder
    {
        public String id;
        public String user_id;
        public String parent_id;
        public String range_id;
        public String range_type;
        public String folder_type;
        public String name;
        public transient JsonElement data_content;
        public String description;
        public int mkdate;
        public int chdate;
        public boolean is_visible;
        public boolean is_readable;
        public boolean is_writable;
    }
    public FileRef[] file_refs;
    public static class FileRef
    {
        public String id;
        public String file_id;
        public String folder_id;
        public int downloads;
        public String description;
        public String content_terms_of_use_id;
        public String user_id;
        public String name;
        public int mkdate;
        public int chdate;
        public int size;
        public String mime_type;
        public String storage;
        public boolean is_readable;
        public boolean is_downloadable;
        public boolean is_editable;
        public boolean is_writable;
    }
}
