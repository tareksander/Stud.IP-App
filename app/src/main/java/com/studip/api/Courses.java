package com.studip.api;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import com.studip.Data;
import com.studip.api.rest.StudipCourse;
import com.studip.api.rest.StudipList;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
public class Courses
{
    public static StudipCourse[] ArrayFromList(StudipList l) throws IOException
    {
        StudipCourse[] courses = new StudipCourse[l.pagination.total-l.pagination.offset];
        Iterator<Map.Entry<String, JsonElement>> it = l.collection.entrySet().iterator();
        for (int i = 0;i<courses.length;i++)
        {
            if (! it.hasNext())
            {
                throw new IOException("not enough elements in the list as specified by the pagination");
            }
            try
            {
                courses[i] = Data.gson.fromJson(it.next().getValue(),StudipCourse.class);
            } catch (JsonSyntaxException e)
            {
                throw new IOException("collection element is not a JsonObject");
            }
        }
        return courses;
    }
}
