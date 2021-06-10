package org.studip.unofficial_app.api.plugins.courseware;
import java.io.Serializable;
public class CoursewareSection  implements Serializable
{
    public String name;
    public String id; // for ?selected=
    public CoursewareBlock[] blocks;
}