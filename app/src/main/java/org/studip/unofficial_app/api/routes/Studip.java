package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipLicense;
import org.studip.unofficial_app.api.rest.StudipNews;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface Studip
{
    @GET("api.php/studip/news")
    Call<StudipCollection<StudipNews>> news(@Query("offset") int offset, @Query("limit") int limit);
    
    @GET("api.php/studip/content_terms_of_use_list")
    Call<StudipCollection<StudipLicense>> terms_of_use_list(@Query("offset") int offset, @Query("limit") int limit);

    @GET("api.php/studip/file_system/folder_types")
    Call<String[]> folderTypes();
    
}
