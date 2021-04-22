package org.studip.unofficial_app.api.routes;


import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipCourse;
import org.studip.unofficial_app.api.rest.StudipCourseMember;
import org.studip.unofficial_app.api.rest.StudipFolder;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipNews;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Course
{
    @GET("api.php/course/{cid}")
    Call<StudipCourse> course(@Path("cid") String course);
    
    
    // TODO api.php/course/{cid}/blubber  GET + POST
    // TODO api.php/course/{cid}/events
    
    @GET("api.php/course/{cid}/forum_categories")
    Call<StudipCollection<StudipForumCategory>> forumCategories(@Path("cid") String course, @Query("offset") int offset, @Query("limit") int limit);


    @GET("api.php/course/{cid}/members")
    Call<StudipCollection<StudipCourseMember>> members(@Path("cid") String course, @Query("offset") int offset, @Query("limit") int limit);

    @GET("api.php/course/{cid}/news")
    Call<StudipCollection<StudipNews>> news(@Path("cid") String course, @Query("offset") int offset, @Query("limit") int limit);

    @GET("api.php/course/{cid}/top_folder")
    Call<StudipFolder> folder(@Path("cid") String course);
    
    
}
