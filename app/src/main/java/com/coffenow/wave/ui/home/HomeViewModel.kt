package com.coffenow.wave.ui.home

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


class HomeViewModel : ViewModel() {

    private val _data = MutableLiveData<DBModel>()
    val dataLoaded = _data
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _isAllDataOnlineLoaded = MutableLiveData<Boolean>()
    val isAllDataOnlineLoaded = _isAllDataOnlineLoaded
    private val _message = MutableLiveData<String>()
    val message = _message
    var nextPageToken: String? = null
    var querySearch: String? = null
    var relatedTo: String? = null

    fun getApiDataQuery(){
        clearAll()
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
                            dataLoaded.value?.items.let {
                                if(it!=null){
                                    itemsToParse.addAll(it)
                                }
                                dataLoaded.value = DBModel(itemsToParse)
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

    fun getApiDataRelated(){
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
                            for (items in data.items){
                                itemsToParse.add(DBModel.Items(items.videoId.videoID!!, items.snippet.title, items.snippet.channelTitle, items.snippet.thumbnails.high.url))
                            }
                            dataLoaded.value?.items.let {
                                if(it!=null){
                                    itemsToParse.addAll(it)
                                }
                                dataLoaded.value = DBModel(itemsToParse)
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
        dataLoaded.value?.items.let {
            if(it!=null){
                itemsToParse.addAll(it)
            }
            dataLoaded.value = DBModel(itemsToParse)
        }
    }

    fun clearAll(){
        val itemsToParse = ArrayList<DBModel.Items>()
        _data.value = DBModel(itemsToParse)
    }

    fun noRepeat(){

    }

    companion object {
        private val TAG = HomeViewModel::class.java.simpleName
    }

}

