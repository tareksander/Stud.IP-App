package org.studip.unofficial_app.api.routes;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OAuth
{
    @POST(org.studip.unofficial_app.api.OAuth.request_token_url)
    Call<String> requestToken(@Header("Authorization") String auth);
    
    
    @POST(org.studip.unofficial_app.api.OAuth.access_token_url)
    Call<String> accessToken(@Header("Authorization") String auth);
}
