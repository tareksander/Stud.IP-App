package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.OAuthUtils;

import retrofit2.Call;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface OAuth
{
    @POST(OAuthUtils.request_token_url)
    Call<String> requestToken(@Header("Authorization") String auth);
    
    
    @POST(OAuthUtils.access_token_url)
    Call<String> accessToken(@Header("Authorization") String auth);
}
