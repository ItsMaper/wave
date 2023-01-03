package com.coffenow.wave.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.coffenow.wave.model.OnBackPlayerTime
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class OnBackPlayer : Service() {

    private lateinit var listener: AbstractYouTubePlayerListener
    private lateinit var player:YouTubePlayerView
    private lateinit var opts : IFramePlayerOptions
    private var currentQueue: Int = 0
    private val _isPlaying = MutableLiveData<Boolean>()
    var isPlaying =_isPlaying
    private val _duration = MutableLiveData<OnBackPlayerTime>()
    var duration = _duration
    private val _currentSecond = MutableLiveData<OnBackPlayerTime>()
    var currentSecond = _currentSecond
    var playlist = mutableListOf<String>()

    companion object{
        private val TAG = OnBackPlayer::class.java.simpleName
    }

    override fun onBind(intent: Intent): IBinder? =  null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setterBind()


        return START_STICKY
    }
    private fun setterBind() {
        player = YouTubePlayerView(this)
        opts = IFramePlayerOptions.Builder()
            .controls(0)
            .rel(0)
            .ivLoadPolicy(3)
            .ccLoadPolicy(3)
            .build()
        listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                youTubePlayer.loadVideo(playlist[currentQueue], 0F)
                super.onReady(youTubePlayer)
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                _isPlaying.value = state == PlayerConstants.PlayerState.PLAYING
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                val time = second.toInt()
                _currentSecond.value = OnBackPlayerTime(time, formatTime(time))
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                super.onVideoDuration(youTubePlayer, duration)
                val time = duration.toInt()
                _currentSecond.value = OnBackPlayerTime(time, formatTime(time))
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                currentQueue += 1
                youTubePlayer.loadVideo(playlist[currentQueue], 0F)
            }
        }
        player.enableAutomaticInitialization = false
        player.initialize(listener, false, opts)
        player.enableBackgroundPlayback(true)
    }

    override fun onDestroy() {
        super.onDestroy()
        player.release()
    }

    private fun formatTime(t: Int) : String {
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

}