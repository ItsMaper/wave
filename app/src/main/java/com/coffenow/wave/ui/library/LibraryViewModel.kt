package com.coffenow.wave.ui.library

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coffenow.wave.model.YTModel
import com.coffenow.wave.network.ApiConfig
import com.coffenow.wave.ui.home.HomeViewModel

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LibraryViewModel : ViewModel() {

    private val _playlist = MutableLiveData<YTModel?>()
    val playlist = _playlist
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _message = MutableLiveData<String>()
    val message = _message
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
                "snippet",
                "0bwlDBtGVd0",
                "video",
                "5",
                nextPageToken)
        client.enqueue(/* callback = */ object : Callback<YTModel>{
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
                            _isAllPlaylistLoaded.value = true }
                        if (data.items.isNotEmpty()){ _playlist.value = data }
                    } else {_message.value = "No video"}
                } else { _message.value = response.message() } }

            override fun onFailure(call: Call<YTModel>, t: Throwable)  {
                _isLoading.value = false
                Log.e(TAG, "Failed: ", t)
                _message.value = t.message }
        })
    }

    companion object {
        private val TAG = YTModel::class.java.simpleName
    }

}