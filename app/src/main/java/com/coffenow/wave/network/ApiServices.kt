package com.coffenow.wave.network


import com.coffenow.wave.model.YTModel
import com.coffenow.wave.model.YTModelPlayLists
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiServices {

    @GET("search")
    fun getVideo(
        @Query("part") part: String,
        @Query("q") query: String?,
        @Query("order") order: String,
        @Query("maxResults") maxResults: String,
        @Query("pageToken") pageToken: String?
    ) : Call<YTModel>

    @GET("playlists")
    fun getPlaylist(
        @Query("part") part: String,
        @Query("q") query: String?,
        @Query("type") type: String,
        @Query("maxResults") maxResults: String,
        @Query("pageToken") pageToken: String?
    ) : Call<YTModelPlayLists>
}