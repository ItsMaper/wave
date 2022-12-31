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
    var nextPageToken: String? = null
    private lateinit var Items : DBModel
    lateinit var firsItem : DBModel.Items
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _message = MutableLiveData<String>()
    val message = _message
    private val _isAllDataOnlineLoaded = MutableLiveData<Boolean>()
    val isAllDataOnlineLoaded = _isAllDataOnlineLoaded
    private val _playList = MutableLiveData<DBModel>()
    var playlistData = _playList

    fun getApiData(){
        _isLoading.value = true
        var itemsToParse = ArrayList<DBModel.Items>()
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
                        if (data.items.isNotEmpty()){
                            itemsToParse.add(firsItem)
                            for (items in data.items){
                                val thisItems = DBModel.Items(items.videoId.videoID!!, items.snippet.title, items.snippet.channelTitle, items.snippet.thumbnails.high.url)
                                itemsToParse.add(thisItems)
                            }
                            playlistData.value = DBModel(itemsToParse)
                        }
                    }
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
    }}
