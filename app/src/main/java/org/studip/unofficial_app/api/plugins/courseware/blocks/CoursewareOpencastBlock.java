package org.studip.unofficial_app.api.plugins.courseware.blocks;

import org.studip.unofficial_app.api.plugins.courseware.CoursewareBlock;
import org.studip.unofficial_app.api.plugins.opencast.OpencastVideo;

public class CoursewareOpencastBlock extends CoursewareBlock
{
    public String hostname;
    
    public String ltidata;
    public String id; // the id to get the opencast video
    public OpencastVideo video; // gets set in the dialog when the video data is fetched
    public CoursewareOpencastBlock(String hostname, String id, String ltidata) {
        this.hostname = hostname;
        this.id = id;
        this.ltidata = ltidata;
    }
}