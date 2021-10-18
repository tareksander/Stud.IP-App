package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipCourseWithForumCategories;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipNews;

import java.util.List;

@Dao
public abstract class CourseDao implements BasicDao<StudipCourse>
{
    @Query("SELECT * FROM courses WHERE course_id = :id")
    public abstract StudipCourse get(String id);
    
    @Query("SELECT * FROM news WHERE course_id = :id")
    public abstract StudipNews[] getNews(String id);
    
    @Query("DELETE FROM news WHERE course_id = :id")
    public abstract void deleteNews(String id);
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void updateInsertNews(StudipNews... n);
    
    @Transaction
    public void replaceNews(StudipNews[] n, String id) {
        deleteNews(id);
        updateInsertNews(n);
    }
    
    
    @Query("SELECT * FROM courses ORDER BY `group` ASC")
    public abstract LiveData<StudipCourse[]> observeAll();

    @Query("SELECT * FROM forum_categories WHERE course = :id")
    public abstract StudipForumCategory[] getCategories(String id);
    
    
    @Query("DELETE FROM courses")
    public abstract void deleteAll();
    
    @Transaction
    public void replaceCourses(StudipCourse[] courses) {
        deleteAll();
        updateInsertMultiple(courses);
    }
    
    
    @Query("SELECT * FROM courses WHERE documents IS NOT NULL")
    public abstract StudipCourse[] getDocumentsCourses();
    
    
    @Query("SELECT * FROM courses WHERE documents IS NOT NULL")
    public abstract LiveData<StudipCourse[]> observeDocumentsCourses();
    
    // WARNING: observable queries fire if any of the mentioned tables are changed, not only the object you look at


    @Query("SELECT * FROM courses WHERE (SELECT `begin` FROM semesters WHERE id = start_semester) <= (SELECT `begin` FROM semesters WHERE id = :id) AND (((SELECT `end` FROM semesters WHERE id = end_semester) >= (SELECT `end` FROM semesters WHERE id = :id)) OR end_semester IS NULL) ORDER BY `group` ASC")
    public abstract LiveData<StudipCourse[]> observeSemester(String id);
    
    @Query("SELECT * FROM news WHERE course_id = :id")
    public abstract LiveData<List<StudipNews>> observeNews(String id);

    @Query("SELECT * FROM courses WHERE course_id = :id")
    public abstract LiveData<StudipCourse> observe(String id);

    @Query("SELECT * FROM forum_categories WHERE course = :id")
    public abstract LiveData<StudipForumCategory[]> observeCategories(String id);
    
    
    @Transaction
    @Query("SELECT * FROM courses WHERE course_id = :id")
    public abstract LiveData<StudipCourseWithForumCategories> observeWithCategories(String id);
    
}
