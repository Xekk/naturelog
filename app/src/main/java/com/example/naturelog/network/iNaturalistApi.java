package com.example.naturelog.network;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.*;

public interface iNaturalistApi {

    @Multipart
    @POST("v1/computervision/score_image")
    Call<ScoreResponse> identifySpecies(
            @Part MultipartBody.Part image,
            @Part("lat") double lat,
            @Part("lng") double lng
    );
}