package org.studip.unofficial_app.api.rest;
import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "users")
public class StudipUser  implements Serializable
{
    @NonNull
    @PrimaryKey
    public String user_id = "";
    
    public String username;
    @Embedded
    public Name name;
    public static class Name  implements Serializable
    {
        @ColumnInfo(name = "name_username")
        public String username;
        @ColumnInfo(name = "name_formatted")
        public String formatted;
        @ColumnInfo(name = "name_family")
        public String family;
        @ColumnInfo(name = "name_given")
        public String given;
        @ColumnInfo(name = "name_prefix")
        public String prefix;
        @ColumnInfo(name = "name_suffix")
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
    public String perms;
    public String email;
    public String avatar_small;
    public String avatar_medium;
    public String avatar_normal;
    public String avatar_original;
    public String phone;
    public String homepage;
    public String privadr;
    @Ignore
    public Datafield[] datafields;
    public static class Datafield  implements Serializable
    {
        public String type;
        public String id;
        public String name;
        public String value;
    }

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
        StudipUser that = (StudipUser) o;
        return user_id.equals(that.user_id) &&
                Objects.equals(username, that.username) &&
                Objects.equals(name, that.name) &&
                Objects.equals(perms, that.perms) &&
                Objects.equals(email, that.email) &&
                Objects.equals(avatar_small, that.avatar_small) &&
                Objects.equals(avatar_medium, that.avatar_medium) &&
                Objects.equals(avatar_normal, that.avatar_normal) &&
                Objects.equals(avatar_original, that.avatar_original) &&
                Objects.equals(phone, that.phone) &&
                Objects.equals(homepage, that.homepage) &&
                Objects.equals(privadr, that.privadr);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(user_id, username, name, perms, email, avatar_small, avatar_medium, avatar_normal, avatar_original, phone, homepage, privadr);
    }

    @NonNull
    @Override
    public String toString()
    {
        return name.formatted+" ( "+username+" )";
    }
}
