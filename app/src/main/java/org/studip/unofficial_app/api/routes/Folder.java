package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipCollection;
import org.studip.unofficial_app.api.rest.StudipFolder;

import retrofit2.Call;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface Folder
{
    @DELETE("api.php/folder/{fid}")
    Call<Void> delete(@Path("fid") String folderID);

    @GET("api.php/folder/{fid}")
    Call<StudipFolder> get(@Path("fid") String folderID);
    
    @FormUrlEncoded
    @PUT("api.php/folder/{fid}")
    Call<StudipFolder> put(@Path("fid") String folderID, @Field("name") String name, @Field("description") String description);
    

    @POST("api.php/folder/{source}/copy/{destination}")
    Call<StudipFolder> copy(@Path("source") String source, @Path("destination") String destination);
    
    @POST("api.php/folder/{source}/move/{destination}")
    Call<StudipFolder> move(@Path("source") String source, @Path("destination") String destination);
    
    @GET("api.php/folder/{fid}/files")
    Call<StudipCollection<StudipFolder.FileRef>> getFiles(@Path("fid") String folderID, @Query("offset") int offset, @Query("limit") int limit);


    // TODO api.php/folder/{fid}/permissions


    @GET("api.php/folder/{fid}/subfolders")
    Call<StudipCollection<StudipFolder.SubFolder>> getSubfolders(@Path("fid") String folderID, @Query("offset") int offset, @Query("limit") int limit);

    
    @FormUrlEncoded
    @POST("api.php/folder/{fid}/new_folder")
    Call<StudipFolder> createFolder(@Path("fid") String folderID, @Field("name") String name, @Field("description") String description);
    
}
