package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipCourseWithForumCategories;
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
    
    @Query("SELECT * FROM courses ORDER BY `group` ASC")
    LiveData<StudipCourse[]> observeAll();

    @Query("SELECT * FROM forum_categories WHERE course = :id")
    StudipForumCategory[] getCategories(String id);
    
    
    @Query("SELECT * FROM courses WHERE documents IS NOT NULL")
    StudipCourse[] getDocumentsCourses();
    
    
    @Query("SELECT * FROM courses WHERE documents IS NOT NULL")
    LiveData<StudipCourse[]> observeDocumentsCourses();
    
    // WARNING: observable queries fire if any of the mentioned tables are changed, not only the object you look at


    @Query("SELECT * FROM courses WHERE (SELECT `begin` FROM semesters WHERE id = start_semester) <= (SELECT `begin` FROM semesters WHERE id = :id) AND (((SELECT `end` FROM semesters WHERE id = end_semester) >= (SELECT `end` FROM semesters WHERE id = :id)) OR end_semester IS NULL) ORDER BY `group` ASC")
    LiveData<StudipCourse[]> observeSemester(String id);
    
    @Query("SELECT * FROM news WHERE course_id = :id")
    LiveData<List<StudipNews>> observeNews(String id);

    @Query("SELECT * FROM courses WHERE course_id = :id")
    LiveData<StudipCourse> observe(String id);

    @Query("SELECT * FROM forum_categories WHERE course = :id")
    LiveData<StudipForumCategory[]> observeCategories(String id);
    
    
    @Transaction
    @Query("SELECT * FROM courses WHERE course_id = :id")
    LiveData<StudipCourseWithForumCategories> observeWithCategories(String id);
    
}
