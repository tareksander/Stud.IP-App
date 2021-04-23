package org.studip.unofficial_app.api.rest;
import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.Expose;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "courses")
public class StudipCourse implements Serializable
{
    @NonNull
    @PrimaryKey
    public String course_id = "";
    
    public String number;
    public String title;
    public String subtitle;
    public String type;
    public String description;
    public String location;
    // because the Gson instance is created to include transient fields, you just have to serialize
    // it manually with Gson when using java serialization
    @Ignore
    public transient JsonObject lecturers; // can't be easily represented in Java
    @Embedded
    public Members members;
    public static class Members  implements Serializable
    {
        public String user;
        public int user_count;
        public String autor;
        public int autor_count;
        public String tutor;
        public int tutor_count;
        public String dozent;
        public int dozent_count;

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
            Members members = (Members) o;
            return user_count == members.user_count &&
                    autor_count == members.autor_count &&
                    tutor_count == members.tutor_count &&
                    dozent_count == members.dozent_count &&
                    Objects.equals(user, members.user) &&
                    Objects.equals(autor, members.autor) &&
                    Objects.equals(tutor, members.tutor) &&
                    Objects.equals(dozent, members.dozent);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(user, user_count, autor, autor_count, tutor, tutor_count, dozent, dozent_count);
        }
    }
    public String start_semester;
    public String end_semester;
    @Ignore
    public transient JsonElement modules; // if empty, it's an empty array, but if there are elements, it's an object !?!
    @Expose(serialize = false, deserialize = false) // this shouldn't get serialized by Gson, modules already is
    @Embedded
    public transient Modules modules_object;
    public static class Modules  implements Serializable
    {
        public String forum;
        public String documents;
        public String wiki;

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
            Modules modules = (Modules) o;
            return Objects.equals(forum, modules.forum) &&
                    Objects.equals(documents, modules.documents) &&
                    Objects.equals(wiki, modules.wiki);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(forum, documents, wiki);
        }
    }
    public int group;

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
        StudipCourse that = (StudipCourse) o;
        return group == that.group &&
                course_id.equals(that.course_id) &&
                Objects.equals(number, that.number) &&
                Objects.equals(title, that.title) &&
                Objects.equals(subtitle, that.subtitle) &&
                Objects.equals(type, that.type) &&
                Objects.equals(description, that.description) &&
                Objects.equals(location, that.location) &&
                Objects.equals(members, that.members) &&
                Objects.equals(start_semester, that.start_semester) &&
                Objects.equals(end_semester, that.end_semester) &&
                Objects.equals(modules_object, that.modules_object);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(course_id, number, title, subtitle, type, description, location, members, start_semester, end_semester, modules_object, group);
    }
}
