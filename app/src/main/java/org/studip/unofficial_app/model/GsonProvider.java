package org.studip.unofficial_app.model;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.lang.reflect.Modifier;

public class GsonProvider
{
    static private Gson gson;
    public static Gson getGson() {
        if (gson == null) {
            // include transient fields, we just don't want java serialization to try to serialize them
            gson = new GsonBuilder().excludeFieldsWithModifiers(Modifier.STATIC).create();
        }
        return gson;
    }
}
