package com.coffenow.wave.activities.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coffenow.wave.model.YTModelPlaylistItem
import com.coffenow.wave.network.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlaylistItemViewModel : ViewModel() {

    private val _playlistItem = MutableLiveData<YTModelPlaylistItem?>()
    val playlistItem = _playlistItem
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _isAllItemLoaded = MutableLiveData<Boolean>()
    val isAllItemLoaded = _isAllItemLoaded
    var nextPageToken: String? = null

    fun getPlaylistItem(playlistId: String) {
        _isLoading.value = true
        val client = ApiConfig
            .getService()
            .getPlaylistItems(
                "snippet,contentDetails",
                playlistId,
                nextPageToken
            )
        client.enqueue(object : Callback<YTModelPlaylistItem> {
            override fun onResponse(
                call: Call<YTModelPlaylistItem>,
                response: Response<YTModelPlaylistItem>
            ) {
                _isLoading.value = false
                val data = response.body()
                if (data != null){
                    if (data.nextPageToken != null){
                        nextPageToken = data.nextPageToken
                    } else {
                        nextPageToken = null
                        _isAllItemLoaded.value = true
                    }
                    if (data.items.isNotEmpty()){
                        _playlistItem.value = data
                    }
                }
            }

            override fun onFailure(call: Call<YTModelPlaylistItem>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "Failure: ", t)
            }
        })
    }

    companion object {
        private val TAG = PlaylistItemViewModel::class.java.simpleName
    }

}