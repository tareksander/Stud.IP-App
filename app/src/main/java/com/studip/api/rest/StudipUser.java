package com.studip.api.rest;

public class StudipUser
{
    // This is just a container for variable to use for deserializing the JSON data with GSON
    public String user_id;
    public String username;
    public Name name;
    public class Name
    {
        public String username;
        public String formatted;
        public String family;
        public String given;
        public String prefix;
        public String suffix;
    }
    public String perms;
    public String email;
    public String avatar_small;
    public String avatar_medium;
    public String avatar_normal;
    public String avatar_original;
    public String phone;
    public String homepage;
    public String privadr;
    public Datafield[] datafields;
    public class Datafield
    {
        public String type;
        public String id;
        public String name;
        public String value;
    }
}
