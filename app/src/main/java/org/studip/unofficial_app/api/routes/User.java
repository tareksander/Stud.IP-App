package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.api.rest.StudipScheduleEntry;
import org.studip.unofficial_app.api.rest.StudipUser;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface User
{
    @GET("api.php/user")
    Call<StudipUser> user();

    // Credentials.basic(username,password)
    @GET("api.php/user")
    Call<StudipUser> login(@Header("Authorization") String encodedCredentials);
    
    @GET("logout.php")
    Call<ResponseBody> logout();
    
    @GET("api.php/user/{uid}")
    Call<StudipUser> user(@Path("uid") String user);

    @GET("api.php/user/{uid}/{box}")
    Call<StudipUser> userBox(@Path("uid") String user, @Path("box") String mailbox);
    
    
    // TODO user/{uid}/activitystream

    // TODO user/{uid}/contacts

    // TODO user/{uid}/contacts/{id}  DELETE + PUT
    
    @GET("api.php/user/{uid}/courses")
    Call<StudipCollection<StudipCourse>> userCourses(@Path("uid") String user, @Query("offset") int offset, @Query("limit") int limit);
    
    // TODO api.php/user/{uid}/courses/{cid}  PATCH


    // TODO api.php/user/{uid}/events

    // TODO api.php/user/{uid}/news

    @GET("api.php/user/{uid}/schedule")
    Call<StudipScheduleEntry[]> userSchedule(@Path("uid") String user);
    
    
    @GET("api.php/user/{uid}/schedule/{sem}")
    Call<StudipScheduleEntry[]> userSchedule(@Path("uid") String user, @Path("sem") String semester);

    @GET("api.php/user/{uid}/top_folder")
    Call<StudipFolder> userFolder(@Path("uid") String user);
}
