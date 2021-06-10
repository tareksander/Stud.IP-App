package org.studip.unofficial_app.api.rest;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "schedule_entry")
public class StudipScheduleEntry implements Serializable
{
    @PrimaryKey
    public int start;
    public int end;
    public String content;
    public String title;
    public String color;
    public String type;

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
        StudipScheduleEntry that = (StudipScheduleEntry) o;
        return start == that.start &&
                end == that.end &&
                Objects.equals(content, that.content) &&
                Objects.equals(title, that.title) &&
                Objects.equals(color, that.color) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(start, end, content, title, color, type);
    }
}
