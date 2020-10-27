package com.studip.api;
import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.studip.Data;
import com.studip.api.rest.StudipCourse;
import com.studip.api.rest.StudipListObject;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
public class Courses
{
    public static StudipCourse[] ArrayFromList(StudipListObject l) throws IOException
    {
        StudipCourse[] courses = new StudipCourse[l.pagination.total-l.pagination.offset];
        Iterator<Map.Entry<String, JsonElement>> it = l.collection.entrySet().iterator();
        for (int i = 0;i<courses.length;i++)
        {
            if (! it.hasNext())
            {
                throw new IOException("not enough elements in the list as specified by the pagination");
            }
            JsonElement el = it.next().getValue();
            try
            {
                courses[i] = Data.gson.fromJson(el,StudipCourse.class);
                if (courses[i].modules.isJsonArray())
                {
                    courses[i].modules_object = null;
                }
                else
                {
                    courses[i].modules_object = Data.gson.fromJson(courses[i].modules,StudipCourse.Modules.class);
                }
            } catch (JsonSyntaxException e)
            {
                //e.printStackTrace();
                //System.err.println(el.toString());
                throw new IOException("collection element is not a JsonObject");
            }
        }
        return courses;
    }
}
