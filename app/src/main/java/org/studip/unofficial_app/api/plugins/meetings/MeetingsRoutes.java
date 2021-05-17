package org.studip.unofficial_app.api.plugins.meetings;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Path;

public interface MeetingsRoutes
{
    @GET("plugins.php/meetingplugin/api/course/{cid}/config")
    Call<MeetingsConfig> getConfig(@Path("cid") String cid);
    
    @GET("plugins.php/meetingplugin/api/course/{cid}/info")
    Call<MeetingsInfo> getInfo(@Path("cid") String cid);
    
    @GET("plugins.php/meetingplugin/api/course/{cid}/rooms")
    Call<MeetingsRoom[]> getRooms(@Path("cid") String cid);
}