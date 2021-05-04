package org.studip.unofficial_app.api.plugins.opencast;

public class OpencastVideo
{
    public String preview_url;
    public String watch_opencast;
    
    public String title;
    public String author;
    public String date;
    public String description;
    
    public VideoVersion[] versions;
    
    public static class VideoVersion {
        public String resolution;
        public String download;
    }
}
