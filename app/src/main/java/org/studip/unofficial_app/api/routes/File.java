package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipFolder;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface File
{
    
    
    
    
    // TODO api.php/file/{fid}  GET + DELETE + PUT
    
    @DELETE("api.php/file/{fid}")
    Call<Void> delete(@Path("fid") String fileID);
    
    
    
    
    // TODO api.php/file/{fid}/copy/{folder}  POST
    
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
