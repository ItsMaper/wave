package com.coffenow.wave.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.coffenow.wave.R
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.OnBackPlayerTime
import com.coffenow.wave.receivers.NotificationReceiver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class OnBackPlayer : LifecycleService() {

    private lateinit var listener: AbstractYouTubePlayerListener
    private lateinit var playerView:YouTubePlayerView
    private lateinit var opts : IFramePlayerOptions
    var player : YouTubePlayer? = null
    private var notificationID: Int = 0
    private var currentTitle :String = ""

    companion object {
        const val CHANNEL_ID = "WavePlayer"
        const val PREV_INTENT_REQUEST = 1
        const val NEXT_INTENT_REQUEST = 2
        const val PAUSE_INTENT_REQUEST = 3

        var fromNotifReturn :MutableLiveData<Boolean> = MutableLiveData(false)
        var playlist = MutableLiveData<DBModel>()
        var currentSoundID = MutableLiveData<String>()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createChannel()
        setObservers()
        if(player == null){
            setterBind()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setObservers() {
        playlist.observeForever {
            it.let { if (!fromNotifReturn.value!!){PlayerActivity.currentQueue.value = 0}else{
                fromNotifReturn.value = false} } }

        PlayerActivity.currentQueue.observeForever { it1 ->
            playlist.value?.let {
                currentTitle = it.items[it1].title
                currentSoundID.value = it.items[it1].id } }

        PlayerActivity.userSecond.observeForever {
            player?.pause()
            player?.seekTo(it)
            player?.play() }

        PlayerActivity.playControl.observeForever {
            if (it) { player?.play() } else { player?.pause() } }

        currentSoundID.observeForever {
            player?.loadVideo(it, 0f)
            with(NotificationManagerCompat.from(this)) {
                notify(notificationID, notificationBuilder().build()) } }
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
                super.onReady(youTubePlayer)
                player = youTubePlayer
                currentSoundID.value?.let { player?.loadVideo(it, 0F) }
            }
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                PlayerActivity.isPlaying.value = state == PlayerConstants.PlayerState.PLAYING
            }
            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                val time = second.toInt()
                if (PlayerActivity.isPlaying.value!!){
                    PlayerActivity.currentSecond.value = OnBackPlayerTime(time)
                }
                if(PlayerActivity.currentSecond.value!!.time == PlayerActivity.duration.value!!.time - 1){
                    PlayerActivity.currentQueue.value = PlayerActivity.currentQueue.value?.plus(1)
                } }
            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                super.onVideoDuration(youTubePlayer, duration)
                val time = duration.toInt()
                PlayerActivity.duration.value = OnBackPlayerTime(time) }
            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                if (error == PlayerConstants.PlayerError.INVALID_PARAMETER_IN_REQUEST|| error == PlayerConstants.PlayerError.VIDEO_NOT_FOUND){
                PlayerActivity.currentQueue.value = PlayerActivity.currentQueue.value!! + 1}
            } }
        playerView.enableAutomaticInitialization = false
        playerView.initialize(listener, false, opts)
        playerView.enableBackgroundPlayback(true)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Wave Player",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun notificationBuilder(): NotificationCompat.Builder {
        val contIntent = Intent(this, PlayerActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val flag = PendingIntent.FLAG_IMMUTABLE
        val contentIntent:PendingIntent = PendingIntent.getActivity(this, 0, contIntent, flag)

        val prevIntent = Intent(this, NotificationReceiver::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        prevIntent.putExtra("notification", "previous")
        val previousIntent = PendingIntent.getBroadcast(this, PREV_INTENT_REQUEST, prevIntent, flag)

        val nxtIntent = Intent(this, NotificationReceiver::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        nxtIntent.putExtra("notification", "next")
        val nextIntent =PendingIntent.getBroadcast(this, NEXT_INTENT_REQUEST, nxtIntent,  flag)

        val pausIntent = Intent(this, NotificationReceiver::class.java).apply { flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK }
        pausIntent.putExtra("notification", "pause")
        val pauseIntent = PendingIntent.getBroadcast(this, PAUSE_INTENT_REQUEST, pausIntent, flag)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID).also {
            it.setSound(null)
            it.setOngoing(true)
            it.setSmallIcon(R.drawable.ic_wave_foreground)
            it.setContentTitle(currentTitle)
            it.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            it.setContentIntent(contentIntent)
            it.addAction(R.drawable.ic_baseline_arrow_back_ios_24_notif, "Previous", previousIntent)
            it.addAction(R.drawable.ic_baseline_motion_photos_paused_24_notif, "Pause", pauseIntent)
            it.addAction(R.drawable.ic_baseline_arrow_forward_ios_24_notif, "Next", nextIntent)
            it.setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView())
            it.priority = NotificationCompat.PRIORITY_DEFAULT
        }

        return builder
    }
}
