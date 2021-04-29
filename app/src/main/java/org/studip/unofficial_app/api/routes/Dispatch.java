package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipNotifications;
import org.studip.unofficial_app.api.rest.StudipSearchUser;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Dispatch
{
    /*
    All these routes are used by javascript and are not part of the REST API.
    They could break in future versions.
     */
    
    @GET("dispatch.php/jsupdater/get")
    Call<StudipNotifications> getNotifications();


    
    // call before trying to search for addressees 
    @GET("dispatch.php/messages/write")
    Call<Void> startMessage();
    
    
    @GET("dispatch.php/multipersonsearch/ajax_search/add_adressees")
    Call<StudipSearchUser[]> searchAddresses(@Query("s") String search);
    
}
