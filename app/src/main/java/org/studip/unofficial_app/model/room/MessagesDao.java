package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.paging.PagingSource;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.studip.unofficial_app.api.rest.StudipMessage;


@Dao
public abstract class MessagesDao implements BasicDao<StudipMessage>
{
    
    @Query("SELECT * FROM messages ORDER BY mkdate DESC")
    public abstract StudipMessage[] getAll();
    
    
    @Query("SELECT * FROM messages WHERE message_id = :id")
    public abstract LiveData<StudipMessage> observe(String id);
    
    
    @Query("SELECT * FROM messages ORDER BY mkdate DESC")
    public abstract LiveData<StudipMessage[]> observeAll();


    @Query("SELECT * FROM messages ORDER BY mkdate DESC")
    public abstract PagingSource<Integer,StudipMessage> getPagedList();
    
    
    
    @Query("DELETE FROM messages")
    public abstract void deleteAll();
    
    @Transaction
    public void replaceMessages(StudipMessage[] m) {
        deleteAll();
        updateInsertMultiple(m);
    }
    
    
}
