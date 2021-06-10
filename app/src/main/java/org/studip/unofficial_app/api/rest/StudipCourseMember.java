package org.studip.unofficial_app.api.rest;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "course_members")
public class StudipCourseMember implements Serializable
{
    @NonNull
    @PrimaryKey
    public String id = "";
    
    public String href;
    @Embedded
    public Name name;
    static class Name implements Serializable {
        public String username;
        public String formatted;
        public String family;
        public String given;
        public String prefix;
        public String suffix;

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
            Name name = (Name) o;
            return Objects.equals(username, name.username) &&
                    Objects.equals(formatted, name.formatted) &&
                    Objects.equals(family, name.family) &&
                    Objects.equals(given, name.given) &&
                    Objects.equals(prefix, name.prefix) &&
                    Objects.equals(suffix, name.suffix);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(username, formatted, family, given, prefix, suffix);
        }
    }
    public String avatar_small;
    public String avatar_medium;
    public String avatar_normal;
    public String avatar_original;
    public String status;
    
    // this is note send in JSON, it has to be set when receiving the CourseMembers of a Course
    public String courseID;

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
        StudipCourseMember that = (StudipCourseMember) o;
        return id.equals(that.id) &&
                Objects.equals(href, that.href) &&
                Objects.equals(name, that.name) &&
                Objects.equals(avatar_small, that.avatar_small) &&
                Objects.equals(avatar_medium, that.avatar_medium) &&
                Objects.equals(avatar_normal, that.avatar_normal) &&
                Objects.equals(avatar_original, that.avatar_original) &&
                Objects.equals(status, that.status) &&
                Objects.equals(courseID, that.courseID);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, href, name, avatar_small, avatar_medium, avatar_normal, avatar_original, status, courseID);
    }
}
