package com.studip_old.api;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import java.util.Iterator;
import java.util.Map;
public class ResponseParser
{
    
    public static JsonObject parseQuery(String query)
    {
        JsonElement root_element = JsonParser.parseString(query);
        if (root_element.isJsonObject())
        {
            return root_element.getAsJsonObject();
        }
        else
        {
            return null;
        }
    }
    
    
    public static String getValue(JsonObject tree, String key)
    {
        Iterator<Map.Entry<String,JsonElement>> it = tree.entrySet().iterator();
        while (it.hasNext())
        {
            Map.Entry<String,JsonElement> entry = it.next();
            if (entry.getKey().equals(key))
            {
                if (entry.getValue().isJsonPrimitive())
                {
                    JsonPrimitive p = entry.getValue().getAsJsonPrimitive();
                    return p.getAsString();
                }
                else
                {
                    return null;
                }
            }
            if (entry.getValue().isJsonObject())
            {
                String ret = getValue(entry.getValue().getAsJsonObject(),key);
                if (ret != null)
                {
                    return ret;
                }
            }
        }
        return null;
    }
    public static JsonObject getCollection(JsonObject tree)
    {
        JsonElement e = tree.get("collection");
        if (e != null)
        {
            if (e.isJsonObject())
            {
                return e.getAsJsonObject();
            }
        }
        return null;
    }
    
    
}
