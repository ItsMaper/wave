package com.coffenow.wave.activities.viewmodel

import android.database.Cursor
import android.util.Log
import androidx.lifecycle.*
import androidx.lifecycle.Observer
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.YTModel
import com.coffenow.wave.network.YTApiConfig
import com.coffenow.wave.ui.home.HomeViewModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.*
import kotlin.collections.ArrayList


class PlayerViewModel : ViewModel() {
    var relatedTo : String?= null
    var querySearch: String? = null
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

    var first : Boolean = true

    fun getApiData(){
        val itemsToParse = ArrayList<DBModel.Items>()
        _isLoading.value = true
        val client = YTApiConfig
            .getService()
            .getVideoRelated(
                "snippet",
                relatedTo,
                "video",
                "50",
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
                            playlistData.value?.items.let {
                                if(it!=null){
                                    itemsToParse.addAll(it)
                                }
                                playlistData.value = DBModel(itemsToParse)
                            }
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

    fun getApiDataQuery(){
        val itemsToParse = ArrayList<DBModel.Items>()
        _isLoading.value = true
        val client = YTApiConfig
            .getService()
            .getVideo(
                "snippet",
                querySearch,
                "video",
                "relevance",
                "50",
                nextPageToken)
        client.enqueue(object : Callback<YTModel>{
            override fun onResponse(call: Call<YTModel>, response: Response<YTModel>) {
                _isLoading.value = false
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null){
                        if (data.nextPageToken != null) { nextPageToken = data.nextPageToken }
                        else { _isAllDataOnlineLoaded.value = true }
                        if (data.items.isNotEmpty()){
                            for (items in data.items){
                                itemsToParse.add(DBModel.Items(items.videoId.videoID!!, items.snippet.title, items.snippet.channelTitle, items.snippet.thumbnails.high.url))
                            }
                            playlistData.value?.items.let {
                                if(it!=null){
                                    itemsToParse.addAll(it)
                                }
                                playlistData.value = DBModel(itemsToParse)
                            }
                        } }
                    else { _message.value = "No Music" }
                } else { _message.value = response.message() } }
            override fun onFailure(call: Call<YTModel>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "Failed: ", t)
                _message.value = t.message }
        })
    }

    fun parseDBData(cursor: Cursor){
        val itemsToParse = ArrayList<DBModel.Items>()
        var i = 0
        while (i < cursor.count-1){
            cursor.moveToPosition(i)
            val id = cursor.getString(0)
            val title = cursor.getString(1)
            val channel = cursor.getString(2)
            val thumb = cursor.getString(3)
            itemsToParse.add(DBModel.Items(id, title, channel, thumb))
            i++
        }
        _playList.value?.items.let {
            if(it!=null){
                itemsToParse.addAll(it)
            }
            _playList.value = DBModel(itemsToParse)
        }
    }


    companion object {
        private val TAG = PlayerViewModel::class.java.simpleName
    }}
