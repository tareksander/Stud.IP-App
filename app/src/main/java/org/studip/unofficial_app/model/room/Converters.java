package org.studip.unofficial_app.model.room;

import androidx.room.TypeConverter;

import org.studip.unofficial_app.model.GsonProvider;

public class Converters
{
    @TypeConverter
    public static String[] fromString(String original) {
        return GsonProvider.getGson().fromJson(original,String[].class);
    }
    
    @TypeConverter
    public static String fromStringArray(String[] strings) {
        return GsonProvider.getGson().toJson(strings);
    }
    
    
    
}
