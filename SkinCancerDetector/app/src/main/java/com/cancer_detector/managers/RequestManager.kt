package com.cancer_detector.managers
import com.cancer_detector.interfaces.ApiConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RequestManager {
    val interceptor = HttpLoggingInterceptor()
    val client = OkHttpClient.Builder().addInterceptor(interceptor).build()


    init {
        //TODO must be None at live
        interceptor.level = HttpLoggingInterceptor.Level.BODY
    }

    //TODO: Change this
    val retrofit = Retrofit.Builder()
            .baseUrl("http://bd213bb11de8.ngrok.io")
            .addConverterFactory(GsonConverterFactory.create())
            .client(client)
            .build()

    val service = retrofit.create(ApiConfig::class.java)

}