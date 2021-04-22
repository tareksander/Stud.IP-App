package com.studip_old.api.plugins;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Meetings
{
    
    
    
    
    
    public Meetings()
    {

        Document d = Jsoup.parse("");
        Element content = d.getElementById("layout_content"); // meetings content
        // all conference rooms
        Elements rooms = content.getElementsByAttributeValue("class","meetingcomponent");
        
        
        
    }
    
    
    
}
