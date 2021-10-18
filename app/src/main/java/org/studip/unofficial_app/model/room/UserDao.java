package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.studip.unofficial_app.api.rest.StudipUser;

import io.reactivex.Single;

@Dao
public abstract class UserDao implements BasicDao<StudipUser>
{
    @Query("SELECT * FROM users")
    public abstract StudipUser[] getAll();

    @Query("SELECT * FROM users")
    public abstract LiveData<StudipUser[]> observeAll();


    @Query("SELECT * FROM users WHERE user_id = :id")
    public abstract StudipUser get(String id);


    @Query("SELECT * FROM users WHERE user_id = :id")
    public abstract Single<StudipUser> getSingle(String id);


    @Query("SELECT * FROM users WHERE user_id = :id")
    public abstract LiveData<StudipUser> observe(String id);



    @Query("SELECT * FROM users WHERE name_formatted LIKE '%:name%' OR username LIKE '%'+:name+'%'")
    public abstract LiveData<StudipUser> search(String name);
    
}
