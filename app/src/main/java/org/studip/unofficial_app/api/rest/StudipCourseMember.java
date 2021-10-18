package org.studip.unofficial_app.api.rest;

import androidx.annotation.NonNull;
import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.io.Serializable;
import java.util.Objects;

@Entity(tableName = "course_members", primaryKeys = {"id", "courseID"})
public class StudipCourseMember implements Serializable
{
    
    @Ignore
    public StudipUser member;
    
    // this is not send in JSON, it has to be set when receiving the CourseMembers of a Course
    @NonNull
    public String courseID = "";
    @NonNull
    public String id = "";
    
    
    public String status;
    
    
    
}
