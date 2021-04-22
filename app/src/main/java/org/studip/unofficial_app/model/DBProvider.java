package org.studip.unofficial_app.model;
import android.content.Context;
import androidx.room.Room;
import org.studip.unofficial_app.model.room.DB;
public class DBProvider
{
    private static DB db;
    public static DB getDB(Context c) {
        if (db == null) {
            db = Room.databaseBuilder(c.getApplicationContext(),DB.class,"db").build();
        }
        return db;
    }
}