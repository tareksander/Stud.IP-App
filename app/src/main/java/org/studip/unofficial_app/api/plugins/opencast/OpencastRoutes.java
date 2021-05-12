package org.studip.unofficial_app.api.plugins.opencast;

import java.util.Map;

import io.reactivex.Single;
import retrofit2.Call;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Query;
import retrofit2.http.Url;

public interface OpencastRoutes
{
    // responds with 500 if the course has no opencast
    
    @GET("plugins.php/opencast/course/index/false")
    Single<String> getOpencastPage(@Query("cid") String cid);
    
    
    // HOSTNAME/search/episode.json, id from video url
    // this has to be used for courses that have opencast in courseware, but don't expose the opencast plugin itself
    @GET
    Call<OpencastQueryResult> queryVideo(@Url String request, @Header("Cookie") String jsessionid);
    
    
    // use the ltidata from the script tag to get a session cookie
    @POST
    @FormUrlEncoded
    Call<Void> lti(@Url String request, @FieldMap Map<String,String> data);
    
    
    
}
