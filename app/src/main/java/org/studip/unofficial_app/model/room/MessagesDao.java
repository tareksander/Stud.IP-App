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
    public abstract StudipMessage get(String id);
    
    @Query("SELECT * FROM messages WHERE message_id = :id")
    public abstract LiveData<StudipMessage> observe(String id);
    
    
    @Query("SELECT * FROM messages ORDER BY mkdate DESC")
    public abstract LiveData<StudipMessage[]> observeAll();
    
    @Query("SELECT * FROM messages WHERE sender = :sender ORDER BY mkdate DESC")
    public abstract LiveData<StudipMessage[]> observeAllSender(String sender);

    @Query("SELECT * FROM messages WHERE NOT sender = :sender ORDER BY mkdate DESC ")
    public abstract PagingSource<Integer,StudipMessage> getPagedListNotSender(String sender);
    
    @Query("SELECT * FROM messages WHERE sender = :sender ORDER BY mkdate DESC")
    public abstract PagingSource<Integer,StudipMessage> getPagedListSender(String sender);
    
    @Query("DELETE FROM messages")
    public abstract void deleteAll();
    
    @Query("DELETE FROM messages WHERE sender = :sender")
    public abstract void deleteSender(String sender);
    
    @Transaction
    public void replaceMessages(StudipMessage[] m) {
        deleteAll();
        updateInsertMultiple(m);
    }
    
    @Transaction
    public void replaceMessagesSender(StudipMessage[] m, String sender) {
        deleteSender(sender);
        updateInsertMultiple(m);
    }
    
    
}
