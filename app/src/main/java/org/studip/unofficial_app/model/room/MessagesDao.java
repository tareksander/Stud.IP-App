package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.paging.DataSource;
import androidx.room.Dao;
import androidx.room.Query;

import org.studip.unofficial_app.api.rest.StudipMessage;


@Dao
public interface MessagesDao extends BasicDao<StudipMessage>
{
    
    @Query("SELECT * FROM messages ORDER BY mkdate DESC")
    StudipMessage[] getAll();
    
    
    @Query("SELECT * FROM messages WHERE message_id = :id")
    LiveData<StudipMessage> observe(String id);
    
    
    @Query("SELECT * FROM messages ORDER BY mkdate DESC")
    LiveData<StudipMessage[]> observeAll();


    @Query("SELECT * FROM messages ORDER BY mkdate DESC")
    DataSource.Factory<Integer,StudipMessage> getPagedList();
    
    
}
