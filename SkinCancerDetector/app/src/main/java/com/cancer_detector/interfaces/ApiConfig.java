package com.cancer_detector.interfaces;

import com.cancer_detector.models.*;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ApiConfig {
    @Multipart
    @POST("/api/predict")
//    Call<response_model> uploadFile(@Part MultipartBody.Part image);

    Call<response_model> uploadFile(@Part MultipartBody.Part file, @Part("file") RequestBody name);
}
