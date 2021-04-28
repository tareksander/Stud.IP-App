package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.model.room.DB;

import java.util.Map;
import java.util.Objects;

import retrofit2.Call;

public class CoursesResource extends NetworkResource<StudipCourse[]> {

    public CoursesResource(Context c)
    {
        super(c);
    }
    
    
    
    @Override
    protected LiveData<StudipCourse[]> getDBData(Context c)
    {
        return DBProvider.getDB(c).courseDao().observeAll();
    }
    
    

    @Override
    protected Call<StudipCollection<StudipCourse>> getCall(Context c)
    {
        return APIProvider.getAPI(c).user.userCourses(APIProvider.getAPI(c).getUserID(),0,1000);
    }

    @Override
    protected void updateDB(Context c, Object o)
    {
        StudipCollection<StudipCourse> res = (StudipCollection<StudipCourse>) o;
        DB db = DBProvider.getDB(c);
        for (Map.Entry<String,StudipCourse> entry : res.collection.entrySet()) {
            StudipCourse course = entry.getValue();
            Gson gson = GsonProvider.getGson();
            // only store the ids, that makes it easier to formulate DB queries
            course.start_semester = lastPathSegment(course.start_semester);
            course.end_semester = lastPathSegment(course.end_semester);
            if (! course.modules.isJsonArray()) {
                try
                {
                    course.modules_object = gson.fromJson(course.modules, StudipCourse.Modules.class);
                } catch (JsonSyntaxException ignored) {}
            }
            db.courseDao().updateInsert(course);
        }
    }
}
