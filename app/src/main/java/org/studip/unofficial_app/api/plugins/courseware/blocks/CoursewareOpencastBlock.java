package org.studip.unofficial_app.api.plugins.courseware.blocks;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareBlock;
public class CoursewareOpencastBlock extends CoursewareBlock
{
    public String hostname;
    public String id; // the id to get the opencast video
    public CoursewareOpencastBlock(String hostname, String id) {
        this.hostname = hostname;
        this.id = id;
    }
}