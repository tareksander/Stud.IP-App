package org.studip.unofficial_app.model.room;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipCourseMember;
import org.studip.unofficial_app.api.rest.StudipEvent;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipForumEntry;
import org.studip.unofficial_app.api.rest.StudipLicense;
import org.studip.unofficial_app.api.rest.StudipMessage;
import org.studip.unofficial_app.api.rest.StudipNews;
import org.studip.unofficial_app.api.rest.StudipScheduleEntry;
import org.studip.unofficial_app.api.rest.StudipSemester;
import org.studip.unofficial_app.api.rest.StudipUser;

@Database(entities = {StudipCourse.class, StudipCourseMember.class, StudipEvent.class, StudipForumCategory.class, StudipForumEntry.class, StudipLicense.class,
                      StudipMessage.class, StudipNews.class, StudipScheduleEntry.class, StudipSemester.class, StudipUser.class},version = 1, exportSchema = false)
public abstract class DB extends RoomDatabase
{
    public abstract CourseDao courseDao();
    public abstract ForumCategoryDao forumCategoryDao();
    public abstract ForumEntryDao forumEntryDao();
    public abstract NewsDao newsDao();
    public abstract UserDao userDao();
    public abstract SemesterDao semesterDao();
}
