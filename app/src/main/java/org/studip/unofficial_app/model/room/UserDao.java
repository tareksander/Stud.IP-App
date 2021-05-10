package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.studip.unofficial_app.api.rest.StudipUser;

import io.reactivex.Single;

@Dao
public interface UserDao extends BasicDao<StudipUser>
{
    @Query("SELECT * FROM users")
    StudipUser[] getAll();

    @Query("SELECT * FROM users")
    LiveData<StudipUser[]> observeAll();


    @Query("SELECT * FROM users WHERE user_id = :id")
    StudipUser get(String id);


    @Query("SELECT * FROM users WHERE user_id = :id")
    Single<StudipUser> getSingle(String id);


    @Query("SELECT * FROM users WHERE user_id = :id")
    LiveData<StudipUser> observe(String id);



    @Query("SELECT * FROM users WHERE name_formatted LIKE '%:name%' OR username LIKE '%'+:name+'%'")
    LiveData<StudipUser> search(String name);
    
}
