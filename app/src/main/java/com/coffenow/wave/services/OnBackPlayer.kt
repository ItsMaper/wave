package com.coffenow.wave.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.coffenow.wave.activities.PlayerActivity

class OnBackPlayer : Service() {


    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
    }
}