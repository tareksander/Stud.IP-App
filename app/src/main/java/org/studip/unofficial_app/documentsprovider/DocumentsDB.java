package org.studip.unofficial_app.documentsprovider;
import androidx.room.Database;
import androidx.room.RoomDatabase;
@Database(entities = {DocumentRoot.class}, version = 1, exportSchema = false)
public abstract class DocumentsDB extends RoomDatabase
{
    public abstract DocumentsDAO documents();
}