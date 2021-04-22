package org.studip.unofficial_app.model;
import android.content.Context;
import androidx.lifecycle.LiveData;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipNews;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
public class NewsResource extends NetworkResource<List<StudipNews>, StudipCollection<StudipNews>>
{
    public NewsResource(Context c)
    {
        super(c);
    }

    @Override
    protected LiveData<List<StudipNews>> getDBData(Context c)
    {
        return DBProvider.getDB(c).newsDao().observeGlobal();
    }

    @Override
    protected Call<StudipCollection<StudipNews>> getCall(Context c)
    {
        return APIProvider.getAPI(c).studip.news(0,1000);
    }

    @Override
    protected void updateDB(Context c, StudipCollection<StudipNews> res)
    {
        for (Map.Entry<String, StudipNews> stringStudipNewsEntry : res.collection.entrySet())
        {
            DBProvider.getDB(c).newsDao().updateInsert(stringStudipNewsEntry.getValue());
        }
    }
}
