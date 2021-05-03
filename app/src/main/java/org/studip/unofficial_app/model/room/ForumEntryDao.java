package org.studip.unofficial_app.model.room;
import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.studip.unofficial_app.api.rest.StudipForumEntry;
import org.studip.unofficial_app.api.rest.StudipForumEntryWithChildren;

@Dao
public interface ForumEntryDao extends BasicDao<StudipForumEntry>
{
    @Query("SELECT * FROM forum_entries WHERE parent_id = :id")
    StudipForumEntry[] getChildren(String id);

    // WARNING: observable queries fire if any of the mentioned tables are changed, not only the object you look at
    
    
    // observes a StudipForumEntry and its children
    @Transaction
    @Query("SELECT * FROM forum_entries WHERE topic_id = :id")
    LiveData<StudipForumEntryWithChildren> observeThread(String id);
    
    
    @Query("SELECT * FROM forum_entries WHERE parent_id = :id")
    LiveData<StudipForumEntry[]> observeChildren(String id);


    @Query("SELECT * FROM forum_entries WHERE topic_id = :id")
    LiveData<StudipForumEntry> observe(String id);
}
