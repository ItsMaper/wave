package com.coffenow.wave.ui.library

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coffenow.wave.model.YTModel
import com.coffenow.wave.network.ApiConfig

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibraryViewModel : ViewModel() {

    private val _playlist = MutableLiveData<YTModel?>()
    val playlist = _playlist
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _isAllPlaylistLoaded = MutableLiveData<Boolean>()
    val isAllPlaylistLoaded = _isAllPlaylistLoaded
    var nextPageToken: String? = null

    init {
        getPlaylist()
    }

    fun getPlaylist() {
        _isLoading.value = true
        val client = ApiConfig
            .getService()
            .getVideoRelated(
                "snippet,contentDetails",
                "0bwlDBtGVd0",
                "video",
                "5",
                nextPageToken)
        client.enqueue(object : Callback<YTModel>{
            override fun onResponse(
                call: Call<YTModel>,
                response: Response<YTModel>
            ) {
                _isLoading.value = false
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null){
                        if (data.nextPageToken != null){
                            nextPageToken = data.nextPageToken
                        } else {
                            nextPageToken = null
                            _isAllPlaylistLoaded.value = true
                        }
                        if (data.items.isNotEmpty()){
                            _playlist.value = data
                        }
                    }
                }

            }

            override fun onFailure(call: Call<YTModel>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "Failure: ", t)
            }
        })
    }

    companion object {
        private val TAG = YTModel::class.java.simpleName
    }

}