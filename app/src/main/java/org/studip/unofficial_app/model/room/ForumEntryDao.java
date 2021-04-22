package org.studip.unofficial_app.model.room;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import org.studip.unofficial_app.api.rest.StudipForumEntry;
@Dao
public interface ForumEntryDao extends BasicDao<StudipForumEntry>
{
    @Query("SELECT * FROM forum_entries WHERE parent_id = :id")
    StudipForumEntry[] getChildren(String id);

    // WARNING: observable queries fire if any of the mentioned tables are changed, not only the object you look at

    @Query("SELECT * FROM forum_entries WHERE parent_id = :id")
    LiveData<StudipForumEntry[]> observeChildren(String id);
}
