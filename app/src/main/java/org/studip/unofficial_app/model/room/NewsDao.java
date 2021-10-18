package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.studip.unofficial_app.api.rest.StudipNews;

import java.util.List;
@Dao
public abstract class NewsDao implements BasicDao<StudipNews>
{
    @Query("SELECT * FROM news WHERE course_id == \"\" ORDER BY chdate DESC")
    public abstract StudipNews[] getGlobal();


    // WARNING: observable queries fire if any of the mentioned tables are changed, not only the object you look at

    @Query("SELECT * FROM news WHERE course_id == \"\" ORDER BY chdate DESC")
    public abstract LiveData<List<StudipNews>> observeGlobal();
    
    @Query("DELETE FROM news WHERE course_id == \"\" ")
    public abstract void deleteGlobal();
    
    @Transaction
    public void replaceGlobal(StudipNews[] news) {
        deleteGlobal();
        updateInsertMultiple(news);
    }
}
