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
    var playlistData = _playList

    fun getApiData(){
        _isLoading.value = true
        val itemsToParse = ArrayList<DBModel.Items>()
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
        val toParse = ArrayList<DBModel.Items>()
        toParse.add(firsItem)
        var i = 0
        while (i < cursor.count){
            cursor.moveToPosition(i)
            val id = cursor.getString(0)
            val title = cursor.getString(1)
            val channel = cursor.getString(2)
            val thumb =cursor.getString(3)
            toParse.add(DBModel.Items(id, title, channel, thumb))
            i++
        }
        playlistData.value = DBModel(toParse)
    }

    fun formatTime(t: Int) : String {
        val hours = t / 3600
        val minutes = (t % 3600) / 60
        val seconds = t % 60
        if (hours == 0) {
            return if (minutes<=9){
                if (seconds <= 9) {
                    "0$minutes:0$seconds"
                } else {
                    "0$minutes:$seconds"
                }
            }else{
                if (seconds <= 9) {
                    "$minutes:0$seconds"
                } else {
                    "$minutes:$seconds"
                }}
        } else{
            return if (minutes<=9){
                if (seconds <= 9) {
                    "$hours:0$minutes:0$seconds"
                } else {
                    "$hours:0$minutes:$seconds"
                }
            }else{
                if (seconds <= 9) {
                    "$hours:$minutes:0$seconds"
                } else {
                    "$hours:$minutes:$seconds"
                }}
        }
    }

    companion object {
        private val TAG = PlayerViewModel::class.java.simpleName
    }}
