package com.example.naturelog.network;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface WikipediaApi {
    @GET("page/summary/{title}")
    Call<WikiSummaryResponse> getSummary(@Path("title") String title);
}