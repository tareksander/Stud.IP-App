package com.studip.api.rest;
public class StudipMessage
{
    public String message_id;
    public String subject;
    public String message;
    public String mkdate;
    public String priority;
    public String message_html;
    // public X tags; I don't know what type the dags array is, probably String[]
    public String sender;
    public String[] recipients;
    public boolean unread;
}