package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipFolder;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Streaming;

public interface File
{
    @GET("api.php/file/{fid}")
    Call<StudipFolder.FileRef> get(@Path("fid") String fileID);
    
    
    @FormUrlEncoded
    @PUT("api.php/file/{fid}")
    Call<Void> put(@Path("fid") String fileID, @Field("name") String name, @Field("description") String description);
    
    
    
    @DELETE("api.php/file/{fid}")
    Call<Void> delete(@Path("fid") String fileID);
    
    
    @POST("api.php/file/{fid}/copy/{folder}")
    Call<StudipFolder.FileRef> copy(@Path("fid") String fileID, @Path("folder") String folderID);
    
    @POST("api.php/file/{fid}/move/{folder}")
    Call<StudipFolder.FileRef> move(@Path("fid") String fileID, @Path("folder") String folderID);
    
    
    // WARNING: @Streaming makes it return instantly, and the ResponseBody stream cannot be used on the main thread
    @Streaming
    @GET("api.php/file/{fid}/download")
    Call<ResponseBody> download(@Path("fid") String fileID);

    
    // MultipartBody.Part.createFormData("filename",getFileName(file,requireActivity()), RequestBody.create(data));
    @Multipart
    @POST("api.php/file/{fid}/update")
    Call<Void> update(@Path("fid") String fileID, @Part MultipartBody.Part body);

    // MultipartBody.Part.createFormData("filename",getFileName(file,requireActivity()), RequestBody.create(data));
    @Multipart
    @POST("api.php/file/{fid}")
    Call<StudipFolder.FileRef> upload(@Path("fid") String folderID, @Part MultipartBody.Part body);
    
    
    
}
