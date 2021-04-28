package org.studip.unofficial_app.model;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipForumEntry;
import org.studip.unofficial_app.model.room.DB;
import retrofit2.Call;

public class ForumResource extends NetworkResource<Object>
{
    private final String courseID;
    
    public static class ForumEntry {
        private final String id;
        public String getId()
        {
            return id;
        }
        public enum Type {
            COURSE, CATEGORY, ENTRY
        };
        private Type type;
        public Type getType()
        {
            return type;
        }
        public ForumEntry(String id, Type type)
        {
            this.id = id;
            this.type = type;
        }
    }
    
    @NonNull
    private ForumEntry current = null;
    private final MediatorLiveData<Object> data = new MediatorLiveData<>();
    private LiveData source;
    
    public ForumResource(Context c, String course)
    {
        super(c);
        this.courseID = course;
        current = new ForumEntry(courseID, ForumEntry.Type.COURSE);
    }

    
    public void setEntry(Context c, ForumEntry e) {
        // we need current to be the same as when the request was made, to put the parent id in the database
        if (isRefreshing().getValue()) {
            return;
        }
        current = e;
        if (e == null) {
            current = new ForumEntry(courseID, ForumEntry.Type.COURSE);
        }
        if (source != null) {
            data.removeSource(source);
        }
        DB db = DBProvider.getDB(c);
        switch (e.getType()) {
            case CATEGORY:
                source = db.forumEntryDao().observeChildren(e.getId());
                break;
            case ENTRY:
                source = db.forumEntryDao().observe(e.getId());
                break;
            case COURSE:
                source = db.courseDao().observeCategories(e.getId());
        }
        data.addSource(source, data::setValue);
        refresh(c);
    }

    @Override
    protected LiveData<Object> getDBData(Context c)
    {
        return data;
    }

    @Override
    protected Call getCall(Context c)
    {
        API api = APIProvider.getAPI(c);
        switch (current.getType()) {
            case CATEGORY:
                return api.forum.areas(current.getId(),0,1000);
            case ENTRY:
                return api.forum.getEntry(current.getId());
            case COURSE:
            default:
                return api.course.forumCategories(current.getId(),0,1000);
        }
    }

    @Override
    protected void updateDB(Context c, Object res)
    {
        DB db = DBProvider.getDB(c);
        if (res instanceof StudipCollection) {
            try {
                StudipCollection<StudipForumCategory> col = (StudipCollection<StudipForumCategory>) res;
                for (StudipForumCategory cat : col.collection.values()) {
                    cat.course = lastPathSegment(cat.course);
                    db.forumCategoryDao().updateInsert(cat);
                }
            } catch (ClassCastException ignored) {
                StudipCollection<StudipForumEntry> col = (StudipCollection<StudipForumEntry>) res;
                for (StudipForumEntry e : col.collection.values()) {
                    e.user = lastPathSegment(e.user);
                    e.course = lastPathSegment(e.course);
                    e.parent_id = current.getId();
                    db.forumEntryDao().updateInsert(e);
                }
            }
        } else {
            StudipForumEntry e = (StudipForumEntry) res;
            e.user = lastPathSegment(e.user);
            e.course = lastPathSegment(e.course);
            e.parent_id = current.getId();
            db.forumEntryDao().updateInsert(e);
        }
    }
}
