package org.studip.unofficial_app.api.rest;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

@Entity(tableName = "forum_entries")
public class StudipForumEntry implements Serializable, Comparable<StudipForumEntry>
{
    @NonNull
    @PrimaryKey
    public String topic_id = "";
    
    public String mkdate;
    public String chdate;
    public String anonymous;
    public String depth;
    public String subject;
    public String user;
    public String course;
    public String content_html;
    public String content;
    @Ignore
    public StudipForumEntry[] children;
    
    // This is to maintain the hierarchy when the entry is stored in Room, because it can't handle arrays.
    // Everything else should ignore this.
    @Expose(serialize = false, deserialize = false)
    @ColumnInfo(name = "parent_id")
    public transient String parent_id = "";

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        StudipForumEntry that = (StudipForumEntry) o;
        return topic_id.equals(that.topic_id) &&
                Objects.equals(mkdate, that.mkdate) &&
                Objects.equals(chdate, that.chdate) &&
                Objects.equals(anonymous, that.anonymous) &&
                Objects.equals(depth, that.depth) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(user, that.user) &&
                Objects.equals(course, that.course) &&
                Objects.equals(content_html, that.content_html) &&
                Objects.equals(content, that.content);
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hash(topic_id, mkdate, chdate, anonymous, depth, subject, user, course, content_html, content);
        return result;
    }
    
    @Override
    public int compareTo(StudipForumEntry o) {
        try {
            return (Integer.parseInt(mkdate) - Integer.parseInt(o.mkdate))*-1;
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}