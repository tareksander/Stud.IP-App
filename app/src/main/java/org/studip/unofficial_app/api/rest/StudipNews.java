package org.studip.unofficial_app.api.rest;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.util.Objects;

@Entity(tableName = "news")
public class StudipNews implements Serializable
{
    @NonNull
    @PrimaryKey
    public String news_id = "";
    
    public String topic;
    public String body;
    public String date;
    public String user_id;
    public String expire;
    public String allow_comments;
    public String chdate;
    public String chdate_uid;
    public String mkdate;
    public String body_html;
    @Ignore
    public String[] ranges;
    
    // used to relate the news to courses
    @Expose(serialize = false, deserialize = false)
    @ColumnInfo(name = "course_id")
    public transient String courseID = "";

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
        StudipNews that = (StudipNews) o;
        return news_id.equals(that.news_id) &&
                Objects.equals(topic, that.topic) &&
                Objects.equals(body, that.body) &&
                Objects.equals(date, that.date) &&
                Objects.equals(user_id, that.user_id) &&
                Objects.equals(expire, that.expire) &&
                Objects.equals(allow_comments, that.allow_comments) &&
                Objects.equals(chdate, that.chdate) &&
                Objects.equals(chdate_uid, that.chdate_uid) &&
                Objects.equals(mkdate, that.mkdate) &&
                Objects.equals(body_html, that.body_html) &&
                Objects.equals(courseID, that.courseID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(news_id, topic, body, date, user_id, expire, allow_comments, chdate, chdate_uid, mkdate, body_html, courseID);
    }
}
