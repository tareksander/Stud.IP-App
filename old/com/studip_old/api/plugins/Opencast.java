package com.studip_old.api.plugins;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

public class Opencast
{
    
    
    public Opencast()
    {
        Document d = Jsoup.parse("");
        // to get all download dialogs on the opencast plugin page:
        d.body().getElementsByAttributeValueContaining("class","ui-dialog ui-corner-all ui-widget ui-widget-content ui-front ocDownload ui-draggable ui-resizable");
        // then you can extract the individual download buttons and their URLs, present them to the user in a dialog, and launch a VideoView if one is selected 
        
        
        
        
        
    }
    
    
    
    
    
}
