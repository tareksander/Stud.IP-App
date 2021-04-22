package org.studip.unofficial_app.api.rest;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import com.google.gson.JsonElement;

import java.util.Arrays;
import java.util.Objects;

@Entity(tableName = "folders")
public class StudipFolder
{
    @PrimaryKey
    public String id;
    public String user_id;
    public String parent_id;
    public String range_id;
    public String range_type;
    public String folder_type;
    public String name;
    @Ignore
    public transient JsonElement data_content;
    public String description;
    public long mkdate;
    public long chdate;
    public boolean is_visible;
    public boolean is_readable;
    public boolean is_writable;
    // in Room you can use a query with the parent id to search for the children, so saving this isn't necessary
    @Ignore
    public SubFolder[] subfolders;
    public static class SubFolder
    {
        public String id;
        public String user_id;
        public String parent_id;
        public String range_id;
        public String range_type;
        public String folder_type;
        public String name;
        public transient JsonElement data_content;
        public String description;
        public long mkdate;
        public long chdate;
        public boolean is_visible;
        public boolean is_readable;
        public boolean is_writable;

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
            SubFolder subFolder = (SubFolder) o;
            return mkdate == subFolder.mkdate &&
                    chdate == subFolder.chdate &&
                    is_visible == subFolder.is_visible &&
                    is_readable == subFolder.is_readable &&
                    is_writable == subFolder.is_writable &&
                    Objects.equals(id, subFolder.id) &&
                    Objects.equals(user_id, subFolder.user_id) &&
                    Objects.equals(parent_id, subFolder.parent_id) &&
                    Objects.equals(range_id, subFolder.range_id) &&
                    Objects.equals(range_type, subFolder.range_type) &&
                    Objects.equals(folder_type, subFolder.folder_type) &&
                    Objects.equals(name, subFolder.name) &&
                    Objects.equals(description, subFolder.description);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id, user_id, parent_id, range_id, range_type, folder_type, name, description, mkdate, chdate, is_visible, is_readable, is_writable);
        }
    }
    // in Room you can use a query with the parent id to search for the children, so saving this isn't necessary
    @Ignore
    public FileRef[] file_refs;
    public static class FileRef
    {
        public String id;
        public String file_id;
        public String folder_id;
        public int downloads;
        public String description;
        public String content_terms_of_use_id;
        public String user_id;
        public String name;
        public long mkdate;
        public long chdate;
        public long size;
        public String mime_type;
        public String storage;
        public boolean is_readable;
        public boolean is_downloadable;
        public boolean is_editable;
        public boolean is_writable;

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
            FileRef fileRef = (FileRef) o;
            return downloads == fileRef.downloads &&
                    mkdate == fileRef.mkdate &&
                    chdate == fileRef.chdate &&
                    size == fileRef.size &&
                    is_readable == fileRef.is_readable &&
                    is_downloadable == fileRef.is_downloadable &&
                    is_editable == fileRef.is_editable &&
                    is_writable == fileRef.is_writable &&
                    Objects.equals(id, fileRef.id) &&
                    Objects.equals(file_id, fileRef.file_id) &&
                    Objects.equals(folder_id, fileRef.folder_id) &&
                    Objects.equals(description, fileRef.description) &&
                    Objects.equals(content_terms_of_use_id, fileRef.content_terms_of_use_id) &&
                    Objects.equals(user_id, fileRef.user_id) &&
                    Objects.equals(name, fileRef.name) &&
                    Objects.equals(mime_type, fileRef.mime_type) &&
                    Objects.equals(storage, fileRef.storage);
        }

        @Override
        public int hashCode()
        {
            return Objects.hash(id, file_id, folder_id, downloads, description, content_terms_of_use_id, user_id, name, mkdate, chdate, size, mime_type, storage, is_readable, is_downloadable, is_editable, is_writable);
        }
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
        StudipFolder that = (StudipFolder) o;
        return mkdate == that.mkdate &&
                chdate == that.chdate &&
                is_visible == that.is_visible &&
                is_readable == that.is_readable &&
                is_writable == that.is_writable &&
                Objects.equals(id, that.id) &&
                Objects.equals(user_id, that.user_id) &&
                Objects.equals(parent_id, that.parent_id) &&
                Objects.equals(range_id, that.range_id) &&
                Objects.equals(range_type, that.range_type) &&
                Objects.equals(folder_type, that.folder_type) &&
                Objects.equals(name, that.name) &&
                Objects.equals(description, that.description);
    }

    @Override
    public int hashCode()
    {
        int result = Objects.hash(id, user_id, parent_id, range_id, range_type, folder_type, name, description, mkdate, chdate, is_visible, is_readable, is_writable);
        return result;
    }
}
