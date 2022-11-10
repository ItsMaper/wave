package com.coffenow.wave.network


import com.coffenow.wave.model.YTModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {

    @GET("search")
    fun getChannel(
        @Query("part") part: String,
        @Query("q") id: String
    ) : Call<YTModel>

}