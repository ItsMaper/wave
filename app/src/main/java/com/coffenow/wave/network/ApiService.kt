package com.coffenow.wave.network

import com.coffenow.wave.ymodel.ChannelModel
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiService {
    @GET("channels")
    fun getChannel(
        @Query("part")part:List<String>,
        @Query("id") id:String
    ): Call<ChannelModel>
}