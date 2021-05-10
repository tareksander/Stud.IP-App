package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.studip.unofficial_app.api.rest.StudipForumEntry;
import org.studip.unofficial_app.api.rest.StudipForumEntryWithChildren;

@Dao
public abstract class ForumEntryDao implements BasicDao<StudipForumEntry>
{
    @Query("SELECT * FROM forum_entries WHERE parent_id = :id")
    public abstract StudipForumEntry[] getChildren(String id);

    
    // deletes all children not passed in the List provided
    
    @Transaction
    public void updateSyncChildren(String parent, StudipForumEntry[] children) {
        String[] ids = new String[children.length];
        for (int i = 0;i<ids.length;i++) {
            ids[i] = children[i].topic_id;
        }
        updateDeleteChildren(parent,ids);
        updateInsertMultiple(children);
    }
    
    
    @Query("DELETE FROM forum_entries WHERE parent_id = :parent AND NOT topic_id IN (:children)")
    protected abstract void updateDeleteChildren(String parent, String[] children);
    
    // WARNING: observable queries fire if any of the mentioned tables are changed, not only the object you look at
    
    
    // observes a StudipForumEntry and its children
    @Transaction
    @Query("SELECT * FROM forum_entries WHERE topic_id = :id")
    public abstract LiveData<StudipForumEntryWithChildren> observeThread(String id);
    
    
    @Query("SELECT * FROM forum_entries WHERE parent_id = :id")
    public abstract LiveData<StudipForumEntry[]> observeChildren(String id);


    @Query("SELECT * FROM forum_entries WHERE topic_id = :id")
    public abstract LiveData<StudipForumEntry> observe(String id);
}
