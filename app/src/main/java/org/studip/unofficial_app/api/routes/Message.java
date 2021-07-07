package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipFolder;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;

public interface Message
{
    @DELETE("api.php/message/{mid}")
    Call<Void> delete(@Path("mid") String id);

    @GET("api.php/message/{mid}")
    Call<Void> get(@Path("mid") String id);
    
    
    // apparently, you cannot really pass a boolean to php via a form
    // true or false get interpreted as true
    // a non-empty string is interpreted as true
    // an empty string is interpreted as false
    @FormUrlEncoded
    @PUT("api.php/message/{mid}")
    Call<Void> update(@Path("mid") String id, @Field("unread") String unread);
    
    @GET("api.php/message/{mid}/file_folder")
    Call<StudipFolder> getFolder(@Path("mid") String id);
    
    
    @FormUrlEncoded
    @POST("api.php/messages")
    Call<Void> create(@Field("subject") String subject, @Field("message") String message, @Field("recipients") String[] userIDs);
    
}
