package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;

import org.studip.unofficial_app.api.rest.StudipSemester;
@Dao
public abstract class SemesterDao implements BasicDao<StudipSemester>
{

    @Query("SELECT * FROM semesters")
    public abstract LiveData<StudipSemester[]> observeAll();
    
    @Query("SELECT * FROM semesters")
    public abstract StudipSemester[] getAll();

    @Query("SELECT * FROM semesters WHERE id = :id")
    public abstract StudipSemester get(String id);


    @Query("SELECT * FROM semesters WHERE `begin` >= :time AND `end` <= :time")
    public abstract StudipSemester getByUnixTime(String time);
}
