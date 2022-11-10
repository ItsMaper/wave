package com.coffenow.wave.ui.home

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.coffenow.wave.model.YTModel
import com.coffenow.wave.network.ApiConfig
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeViewModel : ViewModel() {

    private val _video = MutableLiveData<YTModel?>()
    val video = _video
    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading = _isLoading
    private val _message = MutableLiveData<String>()
    val message = _message

    init {
        getVideoList()
    }

    private fun getVideoList(){
        _isLoading.value = true
        val client = ApiConfig.getService().getChannel("snippet", "Musica Cristiana")
        client.enqueue(object : Callback<YTModel>{
            override fun onResponse(call: Call<YTModel>, response: Response<YTModel>) {
                _isLoading.value = false
                if (response.isSuccessful){
                    val data = response.body()
                    if (data != null && data.items.isNotEmpty()){
                        _video.value = data
                    } else {
                        _message.value = "No video"
                    }
                } else {
                    _message.value = response.message()
                }
            }

            override fun onFailure(call: Call<YTModel>, t: Throwable) {
                _isLoading.value = false
                Log.e(TAG, "Failed: ", t)
                _message.value = t.message
            }
        })
    }

    companion object {
        private val TAG = HomeViewModel::class.java.simpleName
    }

}