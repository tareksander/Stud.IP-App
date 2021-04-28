package org.studip.unofficial_app.model;
import android.content.Context;
import androidx.lifecycle.LiveData;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipNews;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
public class NewsResource extends NetworkResource<List<StudipNews>>
{
    private final String cid;
    public NewsResource(Context c, String cid)
    {
        super(c);
        //System.out.println("news course: "+cid);
        this.cid = cid;
    }

    @Override
    protected LiveData<List<StudipNews>> getDBData(Context c)
    {
        if (cid == null)
        {
            return DBProvider.getDB(c).newsDao().observeGlobal();
        } else {
            return DBProvider.getDB(c).courseDao().observeNews(cid);
        }
    }

    @Override
    protected Call<StudipCollection<StudipNews>> getCall(Context c)
    {
        System.out.println("refeshing news");
        if (cid == null)
        {
            return APIProvider.getAPI(c).studip.news(0, 1000);
        } else {
            return APIProvider.getAPI(c).course.news(cid,0,1000);
        }
    }

    @Override
    protected void updateDB(Context c, Object o)
    {
        StudipCollection<StudipNews> res = (StudipCollection<StudipNews>) o;
        //System.out.println("new news got");
        if (cid != null) {
            for (StudipNews n : res.collection.values()) {
                n.courseID = cid;
            }
        }
        DBProvider.getDB(c).newsDao().updateInsertMultiple(res.collection.values().toArray(new StudipNews[0]));
    }
}
