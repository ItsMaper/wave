package com.coffenow.wave.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.lifecycle.MutableLiveData
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.OnBackPlayerTime
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class OnBackPlayer : Service() {

    private lateinit var listener: AbstractYouTubePlayerListener
    private lateinit var playerView:YouTubePlayerView
    private  var player : YouTubePlayer? = null
    private lateinit var opts : IFramePlayerOptions
    private var playlist = MutableLiveData<DBModel>()
    private var currentSoundID : MutableLiveData<String> = MutableLiveData("")
    private var currentTitle = MutableLiveData<String>()

    override fun onBind(intent: Intent): IBinder? =  null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        setterBind()
        setObservers()
        return START_STICKY
    }
    
    private fun playerNotification(){
        val intent = Intent()
        val pendingIntent: PendingIntent? = TaskStackBuilder.create(this).run {
            addNextIntentWithParentStack(intent)
            getPendingIntent(INTENT_REQUEST, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        notificationStyle = NotificationCompat.Builder(this, channelId).also {
            it.setSmallIcon(R.drawable.ic_wave_foreground)
            it.setContentTitle(intent.getStringExtra("title"))
            it.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            it.addAction(R.drawable.ic_baseline_arrow_back_ios_24_notif, "Previous", pendingIntent)
            it.addAction(R.drawable.ic_baseline_motion_photos_paused_24, "Pause", pendingIntent)
            it.addAction(R.drawable.ic_baseline_arrow_forward_ios_24_notif, "Next", pendingIntent)
            it.setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView())
        }.build()
    }

    private fun setObservers() {
        PlayerActivity.playlistService.observeForever {
            playlist.value = it
        }

        PlayerActivity.currentQueue.observeForever {it1->
            playlist.observeForever {
                currentSoundID.value = it.items[it1].id
            }
        }

        PlayerActivity.userSecond.observeForever {
            player?.seekTo(it)
        }

        PlayerActivity.playControl.observeForever {
            if (it){
                player?.play()
            } else{
                player?.pause()
            }
        }
        currentSoundID.observeForever {
            player?.loadVideo(it, 0f)
        }
    }

    private fun setterBind() {
        playerView = YouTubePlayerView(this)
        opts = IFramePlayerOptions.Builder()
            .controls(0)
            .rel(0)
            .ivLoadPolicy(3)
            .ccLoadPolicy(3)
            .build()
        listener = object : AbstractYouTubePlayerListener() {
            override fun onReady(youTubePlayer: YouTubePlayer) {
                player = youTubePlayer
                youTubePlayer.loadVideo(currentSoundID.value!!, 0F)
                super.onReady(youTubePlayer)
            }

            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                PlayerActivity.isPlaying.value = state == PlayerConstants.PlayerState.PLAYING
            }

            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                val time = second.toInt()
                PlayerActivity.currentSecond.value = OnBackPlayerTime(time, formatTime(time))
                if(PlayerActivity.currentSecond.value!!.seekBar == PlayerActivity.duration.value!!.seekBar){
                    PlayerActivity.currentQueue.value = PlayerActivity.currentQueue.value!! + 1
                }
            }

            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                super.onVideoDuration(youTubePlayer, duration)
                val time = duration.toInt()
                PlayerActivity.duration.value = OnBackPlayerTime(time, formatTime(time))
            }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                PlayerActivity.currentQueue.value = PlayerActivity.currentQueue.value!! + 1
                youTubePlayer.loadVideo(currentSoundID.value!!, 0F)
            }
        }
        playerView.enableAutomaticInitialization = false
        playerView.initialize(listener, false, opts)
        playerView.enableBackgroundPlayback(true)

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
    override fun onDestroy() {
        super.onDestroy()
        playerView.release()
    }
}
