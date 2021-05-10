package org.studip.unofficial_app.api.routes;

import retrofit2.Call;
import retrofit2.http.GET;

public interface TestRoutes
{
    /*
    These routes are only used to make sure the authentication is secure, by testing if authentication data is send to foreign websites
     */
    
    @GET("https://www.google.com/")
    Call<Void> tryGoogle();
    
    
    
    
}
