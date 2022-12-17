package com.coffenow.wave.ui.home

import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coffenow.wave.model.YTModel
import com.coffenow.wave.network.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class HomeViewModel : ViewModel() {

    private val _online_data = MutableLiveData<YTModel?>()
    val online_data = _online_data
    private val _local_data = MutableLiveData<YTModel?>()
    val local_data = _local_data
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _isAllDataOnlineLoaded = MutableLiveData<Boolean>()
    val isAllDataOnlineLoaded = _isAllDataOnlineLoaded
    private val _message = MutableLiveData<String>()
    val message = _message
    var nextPageToken: String? = null
    var querySearch: String? = null

    init { getOnlineList() }
    init { getLocalList() }

    private fun getLocalList() {

    }

    fun getOnlineList(){
        _isLoading.value = true
        val client = ApiConfig
            .getService()
            .getVideo(
                "snippet",
                querySearch,
                "video",
                "relevance",
                "12",
                nextPageToken)
        client.enqueue(object : Callback<YTModel>{
            override fun onResponse(call: Call<YTModel>, response: Response<YTModel>) {
                _isLoading.value = false
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null){
                        if (data.nextPageToken != null) { nextPageToken = data.nextPageToken }
                        else { _isAllDataOnlineLoaded.value = true }
                        if (data.items.isNotEmpty()){ _online_data.value = data } }
                    else { _message.value = "No video" }
                } else { _message.value = response.message() } }
            override fun onFailure(call: Call<YTModel>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "Failed: ", t)
                _message.value = t.message }
        })
    }

    companion object {
        private val TAG = HomeViewModel::class.java.simpleName
    }

}