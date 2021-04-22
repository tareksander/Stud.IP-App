package com.studip_old.api.plugins;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

public class Courseware
{
    
    public Courseware()
    {
        Document d = Jsoup.parse("");
        // get the courseware content
        Element courseware = d.getElementById("courseware");
        Elements content = courseware.getElementsByAttributeValue("class","active-section");
        Elements navigation_bar = courseware.getElementsByAttributeValue("class","active-subchapter stuck");
        // the content contains various blocks:
        // HTML
        // Opencast
        // Test
        // assort: can contain more complex things
        
        
        
        
        
    }
    
    
    
    
}
