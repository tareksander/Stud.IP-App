package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.lifecycle.LiveData;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipNews;

import java.util.List;
import java.util.Objects;

import retrofit2.Call;

public class NewsResource extends NetworkResource<List<StudipNews>>
{
    public boolean empty = true;
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
        //System.out.println("refeshing news");
        if (cid == null)
        {
            return Objects.requireNonNull(APIProvider.getAPI(c)).studip.news(0, 1000);
        } else {
            return Objects.requireNonNull(APIProvider.getAPI(c)).course.news(cid,0,1000);
        }
    }
    
    // cannot be generic for the type, so has to use raw types
    @SuppressWarnings("unchecked")
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
        if (res.collection.size() != 0) {
            empty = false;
        }
        if (cid == null) {
            DBProvider.getDB(c).newsDao().replaceGlobal(res.collection.values().toArray(new StudipNews[0]));
        } else {
            DBProvider.getDB(c).courseDao().replaceNews(res.collection.values().toArray(new StudipNews[0]), cid);
        }
    }
}
