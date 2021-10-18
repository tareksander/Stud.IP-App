package org.studip.unofficial_app.model.room;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Query;
import androidx.room.Transaction;

import org.studip.unofficial_app.api.rest.StudipCourseMember;
import org.studip.unofficial_app.api.rest.StudipCourseMemberWithUser;

@Dao
public abstract class CourseMemberDao implements BasicDao<StudipCourseMember>
{
    @Query("DELETE FROM course_members WHERE courseID = :cid")
    public abstract void deleteCourse(String cid);
    
    @Transaction
    public void replaceCourse(StudipCourseMember[] ms, String cid) {
        deleteCourse(cid);
        updateInsertMultiple(ms);
    }
    
    @Query("SELECT * FROM course_members WHERE courseID = :cid ORDER BY status = 'dozent' DESC, status = 'tutor' DESC, status = 'autor' DESC, (SELECT name_family FROM users WHERE user_id = id) ASC, (SELECT name_given FROM users WHERE user_id = id) ASC")
    public abstract LiveData<StudipCourseMemberWithUser[]> ObserveCourse(String cid);
}
