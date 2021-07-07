package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipNotifications;
import org.studip.unofficial_app.api.rest.StudipSearchUser;

import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Dispatch
{
    /*
    All these routes are used by javascript and are not part of the REST API.
    They could break in future versions.
     */
    
    @GET("dispatch.php/jsupdater/get")
    Call<StudipNotifications> getNotifications();
    
    @GET("/dispatch.php/jsupdater/notifications_seen")
    Call<Void> setNotificationsSeen();
    
    @GET("/dispatch.php/jsupdater/mark_notification_read/{id}")
    Call<Void> setNotificationRead(@Path("id") String id);
    
    
    // call before trying to search for addressees 
    @GET("dispatch.php/messages/write")
    Call<Void> startMessage();
    
    
    @GET("dispatch.php/multipersonsearch/ajax_search/add_adressees")
    Call<StudipSearchUser[]> searchAddresses(@Query("s") String search);
    
    
    
    // MultipartBody.Part.createFormData("filename",filename , RequestBody.create(data));
    // MultipartBody.Part.createFormData("name","message_id" , RequestBody.create(message_id));
    @POST("dispatch.php/messages/upload_attachment")
    @Multipart
    Call<Void> uploadAttachment(@Part MultipartBody.Part body, @Part MultipartBody.Part message);
    
    
    // The token is in a script of the parent forum website. parse that with Jsoup, extract the token
    
    @GET("plugins.php/coreforum/index/index/{id}")
    Call<String> getForumPage(@Path("id") String id, @Query("cid") String course);
    
    // 302 indicates success, Location-Header includes the id of the post (in a URL)
    
    @POST("plugins.php/coreforum/index/add_entry")
    @FormUrlEncoded
    Call<Void> postForumEntry(@Query("cid") String cid, @Field("parent") String parent, @Field("security_token") String xsrf_token,
                              @Field("name") String subject, @Field("content") String content);
    
    
    
    @GET("dispatch.php/settings/notification")
    Call<String> getNotificationSettings();
    
    
    @GET("dispatch.php/settings/notification/open/{id}")
    Call<Void> openNotificationCategory(@Path("id") String id);
    
    
    @POST("dispatch.php/settings/notification/store")
    @FormUrlEncoded
    Call<Void> saveNotificationSettings(@FieldMap Map<String, String> fields);
    
    
}
