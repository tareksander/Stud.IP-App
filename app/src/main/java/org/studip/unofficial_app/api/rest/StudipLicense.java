package org.studip.unofficial_app.api.rest;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.Objects;

@Entity(tableName = "licenses")
public class StudipLicense
{
    @NonNull
    @PrimaryKey
    public String id = "";
    
    public String name;
    public String position;
    public String description;
    public String student_description;
    public String download_condition;
    public String icon;
    public boolean is_default;
    public long mkdate;
    public long chdate;

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
        StudipLicense that = (StudipLicense) o;
        return is_default == that.is_default &&
                mkdate == that.mkdate &&
                chdate == that.chdate &&
                id.equals(that.id) &&
                Objects.equals(name, that.name) &&
                Objects.equals(position, that.position) &&
                Objects.equals(description, that.description) &&
                Objects.equals(student_description, that.student_description) &&
                Objects.equals(download_condition, that.download_condition) &&
                Objects.equals(icon, that.icon);
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(id, name, position, description, student_description, download_condition, icon, is_default, mkdate, chdate);
    }
}
