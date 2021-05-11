package org.studip.unofficial_app.api.plugins.courseware.blocks;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareBlock;
public class CoursewarePDFBlock extends CoursewareBlock
{
    public String url; // without the host
    public String name;
    public CoursewarePDFBlock(String url, String name) {
        this.url = url;
        this.name = name;
    }
}