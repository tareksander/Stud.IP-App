package org.studip.unofficial_app.api.plugins.courseware;
import java.io.Serializable;
public class CoursewareChapter implements Serializable
{
    public String name;
    public String id; // for ?selected=
    public CoursewareSubchapter[] subchapters;
}