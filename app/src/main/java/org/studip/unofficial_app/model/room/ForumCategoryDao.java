package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipForumCategoryWithEntries;

@Dao
public abstract class ForumCategoryDao implements BasicDao<StudipForumCategory>
{
    
    @Transaction
    @Query("SELECT * FROM forum_categories WHERE category_id = :id")
    public abstract LiveData<StudipForumCategoryWithEntries> observeCategoryWithEntries(String id);
    
    
}