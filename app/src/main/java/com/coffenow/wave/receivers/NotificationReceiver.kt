package com.coffenow.wave.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.adapter.PlayerPlaylistAdapter
import com.coffenow.wave.adapter.PlayerPlaylistAdapter.Companion.itemsSize
import com.coffenow.wave.services.OnBackPlayer

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val type = intent?.getStringExtra("notification")
        if (type != null){
            if (type == "previous"){
                PlayerActivity.currentQueue.value?.let {
                    if (it >0){
                        PlayerActivity.currentQueue.value = it-1
                    }
                }
            }
            else if (type == "next"){
                PlayerActivity.currentQueue.value?.let {
                    if (it <= itemsSize.value!!-1){
                        PlayerActivity.currentQueue.value = it+1
                    }else{PlayerActivity.currentQueue.value = 0}
                }
            }
            else if (type== "pause"){
                if (PlayerActivity.isPlaying.value == true){
                    OnBackPlayer().player?.pause()
                }else{
                    OnBackPlayer().player?.play()
                }
            }
        }
    }
}