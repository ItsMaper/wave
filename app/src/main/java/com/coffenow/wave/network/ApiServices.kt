package com.coffenow.wave.network


import com.coffenow.wave.model.YTModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {

    @GET("search")
    fun getVideo(
        @Query("part") part: String,
        @Query("q") query: String?,
        @Query("order") order: String,
        @Query("pageToken") pageToken: String?
    ) : Call<YTModel>

}