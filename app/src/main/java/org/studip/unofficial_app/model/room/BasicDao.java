package org.studip.unofficial_app.model.room;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Update;

import io.reactivex.Completable;

@Dao
public interface BasicDao<T>
{
    @Insert
    void insert(T c);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateInsert(T c);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void updateInsertMultiple(T... c);
    
    @Update
    void update(T c);

    @Delete
    void delete(T c);


    @Insert
    Completable insertAsync(T c);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    Completable updateInsertAsync(T c);

    @Update
    Completable updateAsync(T c);

    @Delete
    Completable deleteAsync(T c);
    
    
}