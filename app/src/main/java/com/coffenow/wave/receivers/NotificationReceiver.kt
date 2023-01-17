package com.coffenow.wave.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.adapter.PlayerPlaylistAdapter
import com.coffenow.wave.adapter.PlayerPlaylistAdapter.Companion.itemsSize
import com.coffenow.wave.services.OnBackPlayer
import com.coffenow.wave.services.OnBackPlayer.Companion.currentQueue
import com.coffenow.wave.services.OnBackPlayer.Companion.fromNotifReturn
import com.coffenow.wave.services.OnBackPlayer.Companion.isBucle
import com.coffenow.wave.services.OnBackPlayer.Companion.isPlaying
import com.coffenow.wave.services.OnBackPlayer.Companion.playControl

class NotificationReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val type = intent?.getStringExtra("notification")
        if (type != null){
            if (type == "previous"){
                currentQueue.value?.let {
                    if (it >0){
                        currentQueue.value = it-1
                    }
                }
            }
            else if (type == "next"){
                currentQueue.value?.let {it1->
                    if (it1 <= itemsSize.value!!-1){
                        currentQueue.value = it1+1
                    }else{isBucle.value?.let {
                        if (it){currentQueue.value = 0}
                    }}
                }
            }
            else if (type== "pause"){
                playControl.value = isPlaying.value != true
            }
        }
    }
}