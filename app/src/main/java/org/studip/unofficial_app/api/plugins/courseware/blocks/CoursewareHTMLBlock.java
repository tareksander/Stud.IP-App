package org.studip.unofficial_app.api.plugins.courseware.blocks;
import org.studip.unofficial_app.api.plugins.courseware.CoursewareBlock;
public class CoursewareHTMLBlock extends CoursewareBlock
{
    public String content;
    // used when serialized to restore the scroll position of the WebView
    public int scrollx = 0;
    public CoursewareHTMLBlock(String content) {
        this.content = content;
    }
}