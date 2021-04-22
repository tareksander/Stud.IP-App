package com.studip_old.api.rest;
import java.io.Serializable;
public class StudipForumEntry implements Serializable
{
    public String topic_id;
    public String mkdate;
    public String chdate;
    public String anonymous;
    public String depth;
    public String subject;
    public String user;
    public String course;
    public String content_html;
    public String content;
    public StudipForumEntry[] children;
}