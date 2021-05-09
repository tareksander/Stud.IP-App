package org.studip.unofficial_app.documentsprovider;

import android.content.Context;

import androidx.room.Room;

public class DocumentsDBProvider
{
    private static DocumentsDB db;
    public static DocumentsDB getDB(Context c) {
        if (db == null) {
            db = Room.databaseBuilder(c,DocumentsDB.class,"docprovider").build();;
        }
        return db;
    }
}
