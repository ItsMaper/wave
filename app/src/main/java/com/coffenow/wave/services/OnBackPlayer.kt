package com.coffenow.wave.services

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.view.View
import android.widget.*
import com.coffenow.wave.R
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class OnBackPlayer : Service(), YouTubePlayerListener {

    private var youtubePlayer: YouTubePlayer? = null
    private lateinit var ytpl:YouTubePlayerView
    private lateinit var playBtn: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var timeTotal : TextView
    private lateinit var currentTime : TextView
    private var maxTime:Float = 0F
    private var curTime:Float = 0F
    private var isPlaying =false
    private var idBase : String? = null

    companion object{
        var isPlaying = false
    }

    override fun onBind(intent: Intent): IBinder? =  null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        idBase = intent?.getStringExtra("idBase")
        val opts = IFramePlayerOptions.Builder()
            .controls(0)
            .rel(0)
            .ivLoadPolicy(3)
            .ccLoadPolicy(3)
            .build()
        ytpl = YouTubePlayerView(this)
        ytpl.enableAutomaticInitialization = false
        ytpl.initialize(this, false, opts)
        ytpl.enableBackgroundPlayback(true)
        return START_STICKY
    }
    private fun setterBind() {

    }
    override fun onDestroy() {
        super.onDestroy()
        ytpl.release()
    }

    override fun onReady(youTubePlayer: YouTubePlayer) {
        val videoId = idBase
        videoId?.let {
            youTubePlayer.loadVideo(it, 0f)
        }
        this.youtubePlayer =youTubePlayer
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        curTime=second
        seekBar.progress = curTime.toInt()
        currentTime.text = formatTime(second.toInt())
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        maxTime = duration
        seekBar.max= maxTime.toInt()
        timeTotal.text= formatTime(duration.toInt())
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        isPlaying = if (state == PlayerConstants.PlayerState.PLAYING ){
            playBtn.setImageResource(R.drawable.ic_baseline_motion_photos_paused_24)
            true
        } else {
            playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            false
        }
        playBtn.setOnClickListener{
            if(state == PlayerConstants.PlayerState.PLAYING){
                youTubePlayer.pause()
                playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            } else if(state==PlayerConstants.PlayerState.PAUSED){
                playBtn.setImageResource(R.drawable.ic_baseline_motion_photos_paused_24)
                youTubePlayer.play()
            }
        }
    }

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}
    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}
    override fun onApiChange(youTubePlayer: YouTubePlayer) {}
    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {}

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