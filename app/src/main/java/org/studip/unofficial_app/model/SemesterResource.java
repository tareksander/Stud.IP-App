package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.lifecycle.LiveData;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipSemester;

import java.util.Objects;

import retrofit2.Call;

public class SemesterResource extends NetworkResource<StudipSemester[]>{


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
        return Objects.requireNonNull(APIProvider.getAPI(c)).semester.semesters(0,1000);
    }
    
    // cannot be generic for the type, so has to use raw types
    @SuppressWarnings("unchecked")
    @Override
    protected void updateDB(Context c, Object o)
    {
        StudipCollection<StudipSemester> res = (StudipCollection<StudipSemester>) o;
        DBProvider.getDB(c).semesterDao().updateInsertMultiple(res.collection.values().toArray(new StudipSemester[0]));
    }
}
