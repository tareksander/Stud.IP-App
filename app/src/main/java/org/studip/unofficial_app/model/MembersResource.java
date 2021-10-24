package org.studip.unofficial_app.model;

import android.content.Context;
import androidx.lifecycle.LiveData;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipCourseMember;
import org.studip.unofficial_app.api.rest.StudipCourseMemberWithUser;
import org.studip.unofficial_app.model.room.UserDao;

import java.util.Objects;

import retrofit2.Call;

public class MembersResource extends NetworkResource<StudipCourseMemberWithUser[]>
{
    private final String cid;
    
    
    public MembersResource(Context c, String cid) {
        super(c);
        this.cid = cid;
    }
    
    @Override
    protected LiveData<StudipCourseMemberWithUser[]> getDBData(Context c) {
        return DBProvider.getDB(c).courseMemberDao().ObserveCourse(cid);
    }
    
    @Override
    protected Call<StudipCollection<StudipCourseMember>> getCall(Context c) {
        return Objects.requireNonNull(APIProvider.getAPI(c)).course.members(cid, 0, 1000);
    }
    
    // cannot be generic for the type, so has to use raw types
    @SuppressWarnings("unchecked")
    @Override
    protected void updateDB(Context c, Object res) {
        StudipCollection<StudipCourseMember> col = (StudipCollection<StudipCourseMember>) res;
        StudipCourseMember[] ms = col.collection.values().toArray(new StudipCourseMember[0]);
        UserDao ud = DBProvider.getDB(c).userDao();
        for (StudipCourseMember m : ms) {
            m.courseID = cid;
            m.id = m.member.user_id;
            m.member.username = m.member.name.username;
            ud.updateInsert(m.member);
        }
        DBProvider.getDB(c).courseMemberDao().replaceCourse(ms, cid);
    }
}
