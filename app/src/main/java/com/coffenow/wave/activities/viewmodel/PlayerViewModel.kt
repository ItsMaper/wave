package com.coffenow.wave.activities.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.YTModel
import com.coffenow.wave.network.YTApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PlayerViewModel : ViewModel() {
    var relatedTo : String?= null
    private val _playList = MutableLiveData<YTModel?>()
    val playlistData = _playList
    var nextPageToken: String? = null
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _message = MutableLiveData<String>()
    val message = _message
    private val _isAllDataOnlineLoaded = MutableLiveData<Boolean>()
    val isAllDataOnlineLoaded = _isAllDataOnlineLoaded

    fun fromApiRestToDBData():ArrayList<DBModel>{
        val playlist = ArrayList<DBModel>()
            for(item in _playList.value!!.items){
                playlist.add(DBModel(item.videoId.videoID!!,item.snippet.title,item.snippet.channelTitle,item
                    .snippet.thumbnails.high.url)) }
        return playlist
    }

    fun getPlayerlist(){
        _isLoading.value = true
        val client = YTApiConfig
            .getService()
            .getVideoRelated(
                "snippet",
                relatedTo,
                "video",
                "15",
                nextPageToken)
        client.enqueue(object : Callback<YTModel> {
            override fun onResponse(call: Call<YTModel>, response: Response<YTModel>) {
                _isLoading.value = false
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null){
                        if (data.nextPageToken != null) { nextPageToken = data.nextPageToken }
                        else { _isAllDataOnlineLoaded.value = true }
                        if (data.items.isNotEmpty()){ playlistData.value = data } }
                    else { _message.value = "No Music" }
                } else { _message.value = response.message() } }
            override fun onFailure(call: Call<YTModel>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "Failed: ", t)
                _message.value = t.message }
        })
    }

    companion object {
        private val TAG = PlayerViewModel::class.java.simpleName
    }
}