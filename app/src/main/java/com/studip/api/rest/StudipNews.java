package com.studip.api.rest;
import java.io.Serializable;
public class StudipNews implements Serializable
{
    public String news_id;
    public String topic;
    public String body;
    public String date;
    public String user_id;
    public String expire;
    public String allow_comments;
    public String chdate;
    public String chdate_uid;
    public String mkdate;
    public String body_html;
    public String[] ranges;
}
