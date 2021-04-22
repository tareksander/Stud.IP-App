package org.studip.unofficial_app.api.rest;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "messages")
public class StudipMessage
{
    @NonNull
    @PrimaryKey
    public String message_id = "";
    
    public String subject;
    public String message;
    public String mkdate;
    public String priority;
    public String message_html;
    // public X tags; I don't know what type the dags array is, probably String[]
    public String sender;
    @Ignore
    public String[] recipients;
    public boolean unread;

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
        StudipMessage that = (StudipMessage) o;
        return unread == that.unread &&
                message_id.equals(that.message_id) &&
                Objects.equals(subject, that.subject) &&
                Objects.equals(message, that.message) &&
                Objects.equals(mkdate, that.mkdate) &&
                Objects.equals(priority, that.priority) &&
                Objects.equals(message_html, that.message_html) &&
                Objects.equals(sender, that.sender);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(message_id, subject, message, mkdate, priority, message_html, sender, unread);
    }
}