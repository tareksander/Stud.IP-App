package org.studip.unofficial_app.documentsprovider;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jetbrains.annotations.NotNull;

@Entity(tableName = "roots")
public class DocumentRoot implements Comparable<DocumentRoot>
{
    @PrimaryKey
    @NonNull
    public String folderID;
    public String title; // title of the root. Course name for courses
    public boolean user; // true for user files
    public String parentID; // id of the user/course
    public boolean enabled;
    
    public DocumentRoot(@NotNull String folderID) {
        this.folderID = folderID;
    }
    
    @Override
    public int compareTo(DocumentRoot o) {
        // just use the folder id, because it's guaranteed to not be null
        return folderID.compareTo(o.folderID);
    }
}