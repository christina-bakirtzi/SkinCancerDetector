package com.example.app.interfaces;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface FileUploadService {
    @Multipart
    @POST("retrofit_example/upload_image.php")
    Call uploadFile(@Part MultipartBody.Part file, @Part("file") RequestBody name);
}
