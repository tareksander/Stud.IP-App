package org.studip.unofficial_app.model.room;

import androidx.annotation.NonNull;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

public class Migrations
{
    public static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE course_members");
            database.execSQL("CREATE TABLE IF NOT EXISTS `course_members` (`courseID` TEXT NOT NULL, `id` TEXT NOT NULL, `status` TEXT, PRIMARY KEY(`id`, `courseID`))");
        }
    };
    
}
