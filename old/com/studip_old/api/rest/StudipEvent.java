package com.studip_old.api.rest;
import java.io.Serializable;
public class StudipEvent  implements Serializable
{
    public String event_id;
    public String start;
    public String end;
    public String title;
    public String description;
    public String categories;
    public String room;
    public String deleted;
    public boolean canceled;
}
