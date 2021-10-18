package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.room.Room;

import org.studip.unofficial_app.model.room.DB;
import org.studip.unofficial_app.model.room.Migrations;

public class DBProvider
{
    private static DB db;
    public static DB getDB(Context c) {
        if (db == null) {
            db = Room.databaseBuilder(c.getApplicationContext(),DB.class,"db").addMigrations(Migrations.MIGRATION_1_2).build();
        }
        return db;
    }
}