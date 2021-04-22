package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipForumCategory;
import org.studip.unofficial_app.api.rest.StudipForumEntry;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Forum
{
    @GET("api.php/forum_category/{category}")
    Call<StudipForumCategory> category(@Path("category") String id);

    // TODO api.php/forum_category/{category}  PUT + DELETE
    
    
    @GET("api.php/forum_category/{category}/areas")
    Call<StudipCollection<StudipForumEntry>> areas(@Path("category") String id, @Query("offset") int offset, @Query("limit") int limit);
    
    
    // TODO api.php/forum_category/{category}/areas  POST


    @GET("api.php/forum_entry/{eid}")
    Call<StudipForumEntry> getEntry(@Path("eid") String id);

    @DELETE("api.php/forum_entry/{eid}")
    Call<StudipForumEntry> deleteEntry(@Path("eid") String id);
    
    
    @FormUrlEncoded
    @POST("api.php/forum_entry/{eid}")
    Call<StudipForumEntry> addEntry(@Path("eid") String id, @Field("subject") String subject, @Field("content") String content);

    @FormUrlEncoded
    @PUT("api.php/forum_entry/{eid}")
    Call<StudipForumEntry> updateEntry(@Path("eid") String id, @Field("subject") String subject, @Field("content") String content);



}
