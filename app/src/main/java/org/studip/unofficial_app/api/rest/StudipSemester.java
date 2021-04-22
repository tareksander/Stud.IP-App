package org.studip.unofficial_app.api.rest;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "semesters")
public class StudipSemester
{
    @NonNull
    @PrimaryKey
    public String id = "";
    
    public String title;
    public String description;
    public long begin;
    public long end;
    public long seminars_begin;
    public long seminars_end;

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
        StudipSemester that = (StudipSemester) o;
        return begin == that.begin &&
                end == that.end &&
                seminars_begin == that.seminars_begin &&
                seminars_end == that.seminars_end &&
                id.equals(that.id) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, title, description, begin, end, seminars_begin, seminars_end);
    }
}
