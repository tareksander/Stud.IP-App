package org.studip.unofficial_app.model;

import android.content.Context;
import androidx.lifecycle.LiveData;
import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipSemester;
import java.util.Map;
import retrofit2.Call;

public class SemesterResource extends NetworkResource<StudipSemester[], StudipCollection<StudipSemester>>{


    public SemesterResource(Context c)
    {
        super(c);
    }

    @Override
    protected LiveData<StudipSemester[]> getDBData(Context c)
    {
        return DBProvider.getDB(c).semesterDao().observeAll();
    }

    @Override
    protected Call<StudipCollection<StudipSemester>> getCall(Context c)
    {
        return APIProvider.getAPI(c).semester.semesters(0,1000);
    }

    @Override
    protected void updateDB(Context c, StudipCollection<StudipSemester> res)
    {
        for (Map.Entry<String,StudipSemester> entry : res.collection.entrySet()) {
            DBProvider.getDB(c).semesterDao().updateInsert(entry.getValue());
        }
    }
}
