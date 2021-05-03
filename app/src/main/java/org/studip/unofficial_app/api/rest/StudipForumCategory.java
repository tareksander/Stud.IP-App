package org.studip.unofficial_app.api.rest;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "forum_categories")
public class StudipForumCategory implements Serializable, Comparable<StudipForumCategory>
{
    @NonNull
    @PrimaryKey
    public String category_id = "";
    
    public String seminar_id;
    public String entry_name;
    public String pos;
    public String id;
    public String course;
    public String areas;
    public int areas_count;

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
        StudipForumCategory that = (StudipForumCategory) o;
        return areas_count == that.areas_count &&
                category_id.equals(that.category_id) &&
                Objects.equals(seminar_id, that.seminar_id) &&
                Objects.equals(entry_name, that.entry_name) &&
                Objects.equals(pos, that.pos) &&
                Objects.equals(id, that.id) &&
                Objects.equals(course, that.course) &&
                Objects.equals(areas, that.areas);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(category_id, seminar_id, entry_name, pos, id, course, areas, areas_count);
    }
    
    @Override
    public int compareTo(StudipForumCategory o) {
        return entry_name.compareTo(o.entry_name);
    }
}