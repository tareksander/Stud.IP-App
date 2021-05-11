package org.studip.unofficial_app.api.plugins.courseware;
import io.reactivex.Single;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;
public interface CoursewareRoutes
{
    @GET("plugins.php/courseware/courseware")
    Single<String> getCourseware(@Query("cid") String cid, @Query("selected") String selected);
}