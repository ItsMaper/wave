package com.coffenow.wave.services

import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.MutableLiveData
import com.coffenow.wave.R
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.activities.PlayerActivity.Companion.currentSecond
import com.coffenow.wave.activities.PlayerActivity.Companion.duration
import com.coffenow.wave.adapter.PlayerPlaylistAdapter
import com.coffenow.wave.db.WaveDBHelper
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.OnBackPlayerTime
import com.coffenow.wave.receivers.NotificationReceiver
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView

class OnBackPlayer : LifecycleService() {

    private lateinit var dbHelper: WaveDBHelper
    private lateinit var listener: AbstractYouTubePlayerListener
    private lateinit var playerView:YouTubePlayerView
    private lateinit var opts : IFramePlayerOptions
    private var notificationID: Int = 0
    private var currentTitle :String = ""

    companion object {
        const val CHANNEL_ID = "WavePlayer"
        const val PREV_INTENT_REQUEST = 1
        const val NEXT_INTENT_REQUEST = 2
        const val PAUSE_INTENT_REQUEST = 3

        var fromNotifReturn = false
        var player : YouTubePlayer? = null
        var playlistService : MutableLiveData<DBModel>?= null
        var currentSoundID = MutableLiveData<String>()
        var currentQueue = MutableLiveData<Int>()
        var isPlaying : MutableLiveData<Boolean> = MutableLiveData(false)
        var isShuffled : MutableLiveData<Boolean> = MutableLiveData(false)
        var isBucle : MutableLiveData<Boolean> = MutableLiveData(false)
        var playControl = MutableLiveData<Boolean>()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)
        createChannel()
        dbHelper = WaveDBHelper(this)
        setObservers()
        if (player== null){
            setterBind()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun setObservers() {
        playlistService?.observeForever {
            it.let { if (!fromNotifReturn){
                currentQueue.value = 0

            } } }

        currentQueue.observeForever { it1 ->
            if (!fromNotifReturn){
                playlistService?.value?.let {
                    dbSearchesUpdater(it.items[it1].id,it.items[it1].title,it.items[it1].channelName,it.items[it1].thumb)
                    currentTitle = it.items[it1].title
                    currentSoundID.value = it.items[it1].id
                } }
             }

        PlayerActivity.userSecond.observeForever {
            if (!fromNotifReturn){
                player?.pause()
                player?.seekTo(it)
                player?.play()
            } }

        playControl.observeForever {
            if (it) { player?.play() } else { player?.pause() } }

        isPlaying.observeForever {
            if (currentTitle!=""){
                with(NotificationManagerCompat.from(this)) {
                    notify(notificationID, notificationBuilder().build()) } } }

        currentSoundID.observeForever {
            if (!fromNotifReturn){
                currentSecond.value = OnBackPlayerTime(0)
                duration.value = OnBackPlayerTime(0)
                player?.loadVideo(it, 0f)
            }}
        fromNotifReturn = false
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
                if(!fromNotifReturn){
                    currentSoundID.value?.let { player?.loadVideo(it, 0F)
                } }
            }
            override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
                super.onStateChange(youTubePlayer, state)
                isPlaying.value = state == PlayerConstants.PlayerState.PLAYING
            }
            override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                super.onCurrentSecond(youTubePlayer, second)
                val time = second.toInt()
                isPlaying.value?.let {
                    if (it){
                        currentSecond.value = OnBackPlayerTime(time)
                    }
                }
                if(currentSecond.value!!.time == duration.value!!.time - 1){
                    isShuffled.value?.let {
                        if (it){
                            val rand: Int = (0 until (PlayerPlaylistAdapter.itemsSize.value!!-1)).random()
                            currentQueue.value = rand
                        }else{
                            currentQueue.value = currentQueue.value?.plus(1)
                        }
                    }

                } }
            override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                super.onVideoDuration(youTubePlayer, duration)
                val time = duration.toInt()
                PlayerActivity.duration.value = OnBackPlayerTime(time) }

            override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {
                super.onError(youTubePlayer, error)
                if (error == PlayerConstants.PlayerError.VIDEO_NOT_PLAYABLE_IN_EMBEDDED_PLAYER|| error == PlayerConstants.PlayerError.VIDEO_NOT_FOUND){
                currentQueue.value = currentQueue.value!! + 1}
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
        } }

    private fun notificationBuilder(): NotificationCompat.Builder {
        val contIntent = Intent(this, PlayerActivity::class.java)
        contIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK ; Intent.FLAG_ACTIVITY_CLEAR_TASK;
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
            it.setSmallIcon(R.drawable.ic_wave_foreground)
            it.setContentTitle(currentTitle)
            it.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            it.setContentIntent(contentIntent)
            it.addAction(R.drawable.icon_player_back_notif, "Previous", previousIntent)
            isPlaying.value?.let { it1->
                if (it1){
                    it.addAction(R.drawable.icon_player_paused_notif, "Pause", pauseIntent)
                    it.setOngoing(true) }else{
                    it.addAction(R.drawable.icon_player_play_notif, "Pause", pauseIntent)
                    it.setOngoing(false) } }
            it.addAction(R.drawable.icon_player_forward_notif, "Next", nextIntent)
            it.setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView())
            it.priority = NotificationCompat.PRIORITY_DEFAULT }
        return builder
    }
    private fun dbSearchesUpdater(id: String?, name:String?, publisher:String?, thumb:String?){
        if(!id.isNullOrEmpty() && !name.isNullOrEmpty() && !publisher.isNullOrEmpty() && !thumb.isNullOrEmpty() ){
            val data = ContentValues()
            data.put("videoID",id)
            data.put("title", name)
            data.put("publisher", publisher)
            data.put("thumbnail", thumb)
            dbHelper.addData("searches", data)
        }
    }
}
