package org.studip.unofficial_app.model;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.Observer;

import com.google.gson.Gson;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipCourse;

import java.util.Map;
import java.util.Objects;

import retrofit2.Call;

public class CoursesResource extends NetworkResource<StudipCourse[], StudipCollection<StudipCourse>> {

    private String semester = null;
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
    protected void updateDB(Context c, StudipCollection<StudipCourse> res)
    {
        for (Map.Entry<String,StudipCourse> entry : res.collection.entrySet()) {
            StudipCourse course = entry.getValue();
            Gson gson = GsonProvider.getGson();
            if (! course.modules.isJsonArray()) {
                course.modules_object = gson.fromJson(course.modules,StudipCourse.Modules.class);
            }
            /*
            System.out.println(course.title);
            if (course.modules_object != null)
            {
                System.out.println(course.modules_object.documents);
                System.out.println(course.modules_object.forum);
                System.out.println(course.modules_object.wiki);
            }
            */
            DBProvider.getDB(c).courseDao().updateInsert(course);
        }
    }
}
