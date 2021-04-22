package org.studip.unofficial_app.api.rest;
import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "events")
public class StudipEvent implements Serializable
{
    @NonNull
    @PrimaryKey
    public String event_id = "";
    
    public String start;
    public String end;
    public String title;
    public String description;
    public String categories;
    public String room;
    public String deleted;
    public boolean canceled;

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
        StudipEvent that = (StudipEvent) o;
        return canceled == that.canceled &&
                event_id.equals(that.event_id) &&
                Objects.equals(start, that.start) &&
                Objects.equals(end, that.end) &&
                Objects.equals(title, that.title) &&
                Objects.equals(description, that.description) &&
                Objects.equals(categories, that.categories) &&
                Objects.equals(room, that.room) &&
                Objects.equals(deleted, that.deleted);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(event_id, start, end, title, description, categories, room, deleted, canceled);
    }
}
