package org.studip.unofficial_app.model.room;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipNews;

import java.util.List;

@Dao
public interface CourseDao extends BasicDao<StudipCourse>
{
    @Query("SELECT * FROM courses WHERE course_id = :id")
    StudipCourse get(String id);
    
    @Query("SELECT * FROM news WHERE course_id = :id")
    StudipNews[] getNews(String id);
    
    @Query("SELECT * FROM courses")
    LiveData<StudipCourse[]> observeAll();

    @Query("SELECT * FROM forum_categories WHERE course LIKE '%'+:id")
    StudipForumCategory[] getCategories(String id);
    
    
    @Query("SELECT * FROM courses WHERE (SELECT `begin` FROM semesters WHERE id = SUBSTR(start_semester,19)) <= (SELECT `begin` FROM semesters WHERE id = :id) AND (SELECT `end` FROM semesters WHERE id = SUBSTR(end_semester,19)) >= (SELECT `end` FROM semesters WHERE id = :id)")
    StudipCourse[] getSemester(String id);
    
    
    
    
    
    // WARNING: observable queries fire if any of the mentioned tables are changed, not only the object you look at


    @Query("SELECT * FROM courses WHERE (SELECT `begin` FROM semesters WHERE id = SUBSTR(start_semester,19)) <= (SELECT `begin` FROM semesters WHERE id = :id) AND (SELECT `end` FROM semesters WHERE id = SUBSTR(end_semester,19)) >= (SELECT `end` FROM semesters WHERE id = :id)")
    LiveData<StudipCourse[]> observeSemester(String id);
    
    @Query("SELECT * FROM news WHERE course_id = :id")
    LiveData<List<StudipNews>> observeNews(String id);

    @Query("SELECT * FROM courses WHERE course_id = :id")
    LiveData<StudipCourse> observe(String id);

    @Query("SELECT * FROM forum_categories WHERE course LIKE '%'+:id")
    LiveData<StudipForumCategory[]> observeCategories(String id);
}
