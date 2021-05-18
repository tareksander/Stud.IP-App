package org.studip.unofficial_app.api.plugins.courseware.blocks;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareBlock;
public class CoursewareHTMLBlock extends CoursewareBlock
{
    public String content;
    public CoursewareHTMLBlock(String content) {
        this.content = content;
    }
}