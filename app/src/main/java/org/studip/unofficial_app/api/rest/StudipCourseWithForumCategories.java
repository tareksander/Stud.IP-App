package org.studip.unofficial_app.api.rest;
import androidx.room.Embedded;
import androidx.room.Relation;
import java.util.List;
public class StudipCourseWithForumCategories
{
    @Embedded
    public StudipCourse c;
    @Relation(
            parentColumn = "course_id",
            entityColumn = "seminar_id"
    )
    public List<StudipForumCategory> categories;
}
