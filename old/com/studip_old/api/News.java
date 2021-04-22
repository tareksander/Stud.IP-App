package com.studip_old.api;

import com.google.gson.JsonElement;
import com.google.gson.JsonSyntaxException;
import com.studip_old.Data;
import com.studip_old.api.rest.StudipListObject;
import com.studip_old.api.rest.StudipNews;

import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class News
{
    public static StudipNews[] ArrayFromList(StudipListObject l) throws IOException
    {
        StudipNews[] news = new StudipNews[l.pagination.total-l.pagination.offset];
        Iterator<Map.Entry<String, JsonElement>> it = l.collection.entrySet().iterator();
        for (int i = 0;i<news.length;i++)
        {
            if (! it.hasNext())
            {
                throw new IOException("not enough elements in the list as specified by the pagination");
            }
            JsonElement el = it.next().getValue();
            try
            {
                news[i] = Data.gson.fromJson(el,StudipNews.class);
            } catch (JsonSyntaxException e)
            {
                //e.printStackTrace();
                //System.err.println(el.toString());
                throw new IOException("collection element is not a JsonObject");
            }
        }
        return news;
    }
}
