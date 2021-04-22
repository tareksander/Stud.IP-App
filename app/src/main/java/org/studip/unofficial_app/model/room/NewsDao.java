package org.studip.unofficial_app.model.room;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import org.studip.unofficial_app.api.rest.StudipNews;
import java.util.List;
@Dao
public interface NewsDao extends BasicDao<StudipNews>
{
    @Query("SELECT * FROM news WHERE course_id == \"\" ")
    StudipNews[] getGlobal();


    // WARNING: observable queries fire if any of the mentioned tables are changed, not only the object you look at

    @Query("SELECT * FROM news WHERE course_id == \"\" ")
    LiveData<List<StudipNews>> observeGlobal();
}
