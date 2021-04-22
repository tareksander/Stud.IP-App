package org.studip.unofficial_app.api.routes;

import org.studip.unofficial_app.api.rest.StudipFolder;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Path;

public interface File
{
    
    // TODO api.php/file/{fid}  GET + DELETE + PUT
    
    
    // TODO api.php/file/{fid}/copy/{folder}  POST
    
    @GET("api.php/file/{fid}/download")
    Call<ResponseBody> download(@Path("fid") String fileID);

    
    // new MultipartBody.Builder().addFormDataPart("name","_FILES").addFormDataPart("filename",filename).addPart(RequestBody.create(file)).build();
    @Multipart
    @POST("api.php/file/{fid}/update")
    Call<ResponseBody> update(@Path("fid") String fileID, @Body MultipartBody body);

    // new MultipartBody.Builder().addFormDataPart("name","_FILES").addFormDataPart("filename",filename).addPart(RequestBody.create(file)).build();
    @Multipart
    @POST("api.php/file/{fid}")
    Call<StudipFolder.FileRef> upload(@Path("fid") String folderID, @Body MultipartBody body);
    
    
    
}
