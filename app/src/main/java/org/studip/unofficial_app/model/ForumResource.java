package org.studip.unofficial_app.model;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.SavedStateHandle;

import org.studip.unofficial_app.api.API;
import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipForumEntry;
import org.studip.unofficial_app.model.room.DB;

import java.util.Objects;

import retrofit2.Call;
// cannot be generic for the type, so has to use raw types
@SuppressWarnings("rawtypes")
public class ForumResource extends NetworkResource<Object>
{
    private final String courseID;
    public static final String CURRENT_KEY = "current";
    
    public static class ForumEntry implements Parcelable
    {
        public enum Type {
            COURSE, CATEGORY, ENTRY
        }
        private final Type type;
        private final String id;
        protected ForumEntry(Parcel in) {
            id = in.readString();
            type = Type.values()[in.readInt()];
        }
    
        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(id);
            dest.writeInt(type.ordinal());
        }
    
        @Override
        public int describeContents() {
            return 0;
        }
    
        public static final Creator<ForumEntry> CREATOR = new Creator<ForumEntry>()
        {
            @Override
            public ForumEntry createFromParcel(Parcel in) {
                return new ForumEntry(in);
            }
            
            @Override
            public ForumEntry[] newArray(int size) {
                return new ForumEntry[size];
            }
        };
    
        public String getId()
        {
            return id;
        }
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
    
    
    
    private final LiveData<ForumEntry> currentEntry;
    
    private final MediatorLiveData<Object> data = new MediatorLiveData<>();
    private LiveData source;
    private final SavedStateHandle h;
    
    public ForumResource(Context c, String course, SavedStateHandle h)
    {
        super(c);
        this.courseID = course;
        this.h = h;
        currentEntry = h.getLiveData(CURRENT_KEY, new ForumEntry(courseID, ForumEntry.Type.COURSE));
        // to run the code to determine the database query
        setEntry(c, currentEntry.getValue());
    }
    
    public ForumEntry getSelectedEntry() {
        return currentEntry.getValue();
    }
    
    // cannot be generic for the type, so has to use raw types
    @SuppressWarnings("unchecked")
    public void setEntry(Context c, ForumEntry e) {
        // we need current to be the same as when the request was made, to put the parent id in the database
        LiveData<Boolean> ref = isRefreshing();
        if (ref.getValue() != null && ref.getValue()) {
            return;
        }
        if (e == null) {
            e = new ForumEntry(courseID, ForumEntry.Type.COURSE);
        }
        h.set(CURRENT_KEY, e);
        if (source != null) {
            data.removeSource(source);
        }
        DB db = DBProvider.getDB(c);
        switch (Objects.requireNonNull(currentEntry.getValue()).getType()) {
            case CATEGORY:
                source = db.forumCategoryDao().observeCategoryWithEntries(currentEntry.getValue().getId());
                break;
            case ENTRY:
                source = db.forumEntryDao().observeThread(currentEntry.getValue().getId());
                break;
            case COURSE:
                source = db.courseDao().observeWithCategories(currentEntry.getValue().getId());
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
        API api = Objects.requireNonNull(APIProvider.getAPI(c));
        switch (Objects.requireNonNull(currentEntry.getValue()).getType()) {
            case CATEGORY:
                return api.forum.areas(currentEntry.getValue().getId(),0,1000);
            case ENTRY:
                return api.forum.getEntry(currentEntry.getValue().getId());
            case COURSE:
            default:
                return api.course.forumCategories(currentEntry.getValue().getId(),0,1000);
        }
    }
    
    // cannot be generic for the type, so has to use raw types
    @SuppressWarnings("unchecked")
    @Override
    protected void updateDB(Context c, Object res)
    {
        DB db = DBProvider.getDB(c);
        if (res instanceof StudipCollection) {
            try {
                StudipCollection<StudipForumCategory> col = (StudipCollection<StudipForumCategory>) res;
                for (StudipForumCategory cat : col.collection.values()) {
                    cat.course = lastPathSegment(cat.course);
                    //System.out.println(cat.course);
                    db.forumCategoryDao().updateInsert(cat);
                    //System.out.println("Category: "+cat.entry_name);
                }
            } catch (ClassCastException ignored) {
                StudipCollection<StudipForumEntry> col = (StudipCollection<StudipForumEntry>) res;
                for (StudipForumEntry e : col.collection.values()) {
                    e.user = lastPathSegment(e.user);
                    e.course = lastPathSegment(e.course);
                    e.parent_id = Objects.requireNonNull(currentEntry.getValue()).getId();
                    //System.out.println(e.parent_id);
                    //System.out.println("Entry: "+e.subject);
                    //db.forumEntryDao().updateInsert(e);
                }
                db.forumEntryDao().updateSyncChildren(Objects.requireNonNull(currentEntry.getValue()).getId(),col.collection.values().toArray(new StudipForumEntry[0]));
            }
        } else {
            StudipForumEntry e = (StudipForumEntry) res;
            for (StudipForumEntry child : e.children) {
                child.user = lastPathSegment(child.user);
                child.course = lastPathSegment(child.course);
                child.parent_id = e.topic_id;
                //System.out.println("Child: "+child.subject);
                //db.forumEntryDao().updateInsert(child);
            }
            db.forumEntryDao().updateSyncChildren(Objects.requireNonNull(currentEntry.getValue()).getId(),e.children);
        }
    }
}
