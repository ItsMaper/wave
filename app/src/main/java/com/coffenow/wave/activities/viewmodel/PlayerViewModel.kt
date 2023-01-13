package com.coffenow.wave.activities.viewmodel

import android.database.Cursor
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
    lateinit var firsItem : DBModel.Items
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _message = MutableLiveData<String>()
    val message = _message
    private val _isAllDataOnlineLoaded = MutableLiveData<Boolean>()
    val isAllDataOnlineLoaded = _isAllDataOnlineLoaded
    private val _playList = MutableLiveData<DBModel>()
    private val itemsToParse = ArrayList<DBModel.Items>()
    var playlistData = _playList
    var first : Boolean = true

    fun getApiData(){
        _isLoading.value = true

        val client = YTApiConfig
            .getService()
            .getVideoRelated(
                "snippet",
                relatedTo,
                "video",
                "22",
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
                            if(first){
                                itemsToParse.add(firsItem)
                            }
                            for (items in data.items){
                                val thisItems = DBModel.Items(items.videoId.videoID!!, items.snippet.title, items.snippet.channelTitle, items.snippet.thumbnails.high.url)
                                itemsToParse.add(thisItems)
                            }
                            _playList.value = DBModel(itemsToParse)
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

    fun parseDBData(cursor: Cursor){
        if (first){
            itemsToParse.add(firsItem)
        }
        var i = 0
        while (i < cursor.count){
            cursor.moveToPosition(i)
            val id = cursor.getString(0)
            val title = cursor.getString(1)
            val channel = cursor.getString(2)
            val thumb =cursor.getString(3)
            itemsToParse.add(DBModel.Items(id, title, channel, thumb))
            i++
        }
    }

    fun parse(){
        println(_playList.value)
    }



    companion object {
        private val TAG = PlayerViewModel::class.java.simpleName
    }}
