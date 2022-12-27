package com.coffenow.wave.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AbsListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.view.size
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.coffenow.wave.R
import com.coffenow.wave.activities.viewmodel.PlayerViewModel
import com.coffenow.wave.adapter.PlayerPlaylistAdapter
import com.coffenow.wave.databinding.ActivityPlayerBinding
import com.coffenow.wave.services.OnBackPlayer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions


class PlayerActivity : AppCompatActivity(), YouTubePlayerListener {

    private var _binding: ActivityPlayerBinding? = null
    private val binding get() = _binding!!
    private var playerViewModel:PlayerViewModel?=null
    private val playlistAdapter=PlayerPlaylistAdapter()
    private lateinit var rvPLaylist: RecyclerView
    private lateinit var playerTitle: TextView
    private lateinit var playerPublisher: TextView
    private lateinit var playerThumbnail: ImageView
    private lateinit var playBtn:ImageButton
    private lateinit var prevBtn:ImageButton
    private lateinit var nextBtn:ImageButton
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var timeTotal : TextView
    private lateinit var currentTime : TextView
    private lateinit var notificationStyle: Notification
    private lateinit var pPlaylistLayout: LinearLayout
    private lateinit var queueText: TextView
    private val notificationStyleID = 0
    private val channelName = "channelName"
    private val channelId = "channelId"
    private var youtubePlayer:YouTubePlayer? = null
    private var isPlaying =false
    private var maxTime:Float = 0F
    private var curTime:Float = 0F
    private var isLoading = false
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1
    private var isAllVideoLoaded = false
    private var isPlaylistFullScreen = false
    private var isFirst = true
    private var first = true

    companion object {
        const val INTENT_REQUEST = 0
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        playerViewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        return super.onCreateView(name, context, attrs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        setterBind()
        val type:String= intent.getStringExtra("type").toString()
        if (type=="local") setLocalPlayer() else if(type == "web") setWebPlayer()
        initRecyclerView()
        //playerNotification()
        //val notifManager = NotificationManagerCompat.from(this)
        //notifManager.notify(notificationStyleID, notificationStyle)
        initAdsView()
    }
    private fun initAdsView() {
        MobileAds.initialize(this) {}
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }
    private fun setterBind() {
        binding.ytpl.visibility = INVISIBLE
        rvPLaylist = binding.rvPPlaylist
        playerTitle = binding.playerTitle
        playerPublisher = binding.playerPublisher
        queueText = binding.queueText
        playerThumbnail= binding.playerThumbnail
        playBtn = binding.playerPlay
        prevBtn = binding.playerPrev
        nextBtn = binding.playerNext
        seekBar = binding.playerSeek
        timeTotal = binding.timeText
        currentTime = binding.currentTimeText
        pPlaylistLayout = binding.PPlaylistLayout
        playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        prevBtn.setImageResource(R.drawable.ic_baseline_arrow_back_ios_24)
        nextBtn.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24)
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


    @SuppressLint("SetTextI18n")
    private fun initRecyclerView() {
        playerViewModel?.relatedTo = intent.getStringExtra("id")
        playerViewModel?.getPlayerlist()

        val manager = LinearLayoutManager(this)
        rvPLaylist.apply {
            adapter = playlistAdapter
            layoutManager = manager
            binding.queueText.text="8 Songs"
            addOnScrollListener(object :RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                        isScroll = true } }
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    currentItem = manager.childCount
                    totalItem = manager.itemCount
                    scrollOutItem = manager.findFirstVisibleItemPosition()
                    if (isScroll && (currentItem + scrollOutItem == totalItem)){
                        isScroll = false
                        if (!isLoading && totalItem<=23){
                            playerViewModel?.getPlayerlist() } } } } )}



        playlistAdapter.addListener = PlayerPlaylistAdapter.ItemClickListener { data ->
            data.videoId.videoID.let { id ->
                id?.let { youtubePlayer?.loadVideo(it, 0f) }
            }
            playerTitle.text = data.snippet.title
            playerPublisher.text = data.snippet.channelTitle
            queueText.text = data.snippet.title
            Glide.with(binding.root)
                .load(data.snippet.thumbnails.high.url)
                .dontAnimate()
                .dontTransform()
                .into(playerThumbnail)
        }

        playerViewModel?.playList_data?.observe(this) {
            it?.items?.get(0)?.videoId?.videoID?.let { it1 ->
                playlistAdapter.setDataDiff(it.items, rvPLaylist)
            }
        }
        pPlaylistLayout.setOnClickListener {
            if (isPlaylistFullScreen){
                it.translationY = 0F
                isPlaylistFullScreen = false
                queueText.text = "$totalItem Songs"
            }else{
                it.translationY = -(binding.root.height-(playerThumbnail.height*1.5)).toFloat()
                isPlaylistFullScreen = true
                queueText.text = binding.playerTitle.text
            }
        }
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

    private fun setLocalPlayer() {
        binding.playerPublisher.visibility = INVISIBLE
        val title = intent.getStringExtra("title")
        val path = intent.getStringExtra("path")
        val uri = intent.getStringExtra("uri")
        mediaPlayer(uri.toString())
        binding.playerTitle.text = title
    }
    private fun mediaPlayer(url: String) {
        try {
            mediaPlayer= MediaPlayer().apply {
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                setDataSource(url)
                prepareAsync()
            }
            mediaPlayer.setOnPreparedListener{
                playQ(this)
            }
            setLocalSeekBar()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }

    private fun setLocalSeekBar() {
        var currentPosition = mediaPlayer.currentPosition
        val total = mediaPlayer.duration
        seekBar.max = total

        if (mediaPlayer.isPlaying) {
            mediaPlayer.seekTo(seekBar.progress)
        }
        while (mediaPlayer.isPlaying && currentPosition < total) {
            try {
                Thread.sleep(1000)
                currentPosition = mediaPlayer.currentPosition
            } catch (e: InterruptedException) {
                return
            } catch (e: Exception) {
                return
            }

            seekBar.progress = currentPosition
        }
    }

    private fun playQ(context: Context) {
        playBtn.setOnClickListener {
            if(mediaPlayer.isPlaying){
                playBtn.setBackgroundResource(R.drawable.ic_baseline_motion_photos_paused_24)
                mediaPlayer.start()
            }else{
                playBtn.setBackgroundResource(R.drawable.ic_baseline_play_circle_outline_24)
                mediaPlayer.stop()
            }
        }
    }

    //WEB!
    private fun setWebPlayer() {
        playerPublisher.visibility = VISIBLE
        if (isFirst){
            playerTitle.text = intent.getStringExtra("title")
            playerPublisher.text = intent.getStringExtra("publisher")
            Glide.with(binding.root)
                .load(intent.getStringExtra("thumbnail"))
                .into(playerThumbnail)

            isFirst=false
        }
        nextBtn.setOnClickListener {webQueueManagement(true)}
        prevBtn.setOnClickListener {webQueueManagement(false)}
        val opts = IFramePlayerOptions.Builder()
            .controls(0)
            .rel(0)
            .ivLoadPolicy(3)
            .ccLoadPolicy(3)
            .build()

        val ytpl = binding.ytpl
        ytpl.initialize(this, false, opts)
        ytpl.enableBackgroundPlayback(true)
    }

    private fun setWebSeekBar(){
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            if(fromUser){
                youtubePlayer?.seekTo(progress.toFloat())
            }else{
                if (seekBar?.progress  == seekBar?.max){
                    webQueueManagement(true) } }}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {} } )

    }
    private fun webQueueManagement(isNext : Boolean) {
        var currentQueue = playlistAdapter.currentSelected
        var key = true
        seekBar.progress=0
        playerViewModel?.playList_data?.observe(this) {
            if (currentQueue != null) {
                if (isNext){
                    if(currentQueue>=0 && !first){
                        currentQueue+=1
                    }else {first = false}
                    playlistAdapter.currentSelected = currentQueue
                } else{
                    if (currentQueue >0) {
                        currentQueue-=1
                    }else{
                        playerTitle.text = intent.getStringExtra("title")
                        playerPublisher.text = intent.getStringExtra("publisher")
                        youtubePlayer?.loadVideo(intent.getStringExtra("id")!!, 0f)
                        Glide.with(binding.root)
                            .load(intent.getStringExtra("thumbnail"))
                            .into(playerThumbnail)
                        key =false
                    }
                    playlistAdapter.currentSelected = currentQueue
                }
                if(key){
                    it?.items?.get(currentQueue)?.videoId?.videoID?.let { it1 ->
                        youtubePlayer?.loadVideo(it1, 0f)
                    }
                    playerTitle.text = it?.items?.get(currentQueue)?.snippet?.title
                    playerPublisher.text = it?.items?.get(currentQueue)?.snippet?.channelTitle
                    Glide.with(binding.root)
                        .load(it?.items?.get(currentQueue)?.snippet?.thumbnails?.high?.url)
                        .dontAnimate()
                        .dontTransform()
                        .into(playerThumbnail)
                }
                if (isPlaylistFullScreen){
                    queueText.text=playerTitle.text
                }
            }
        }
    }


    override fun onReady(youTubePlayer: YouTubePlayer) {
        val videoId = intent.getStringExtra("id")
        videoId?.let {
            youTubePlayer.loadVideo(it, 0f)
        }
        this.youtubePlayer =youTubePlayer
        setWebSeekBar()
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

    override fun onStop() {
        super.onStop()
        val i =Intent(this, OnBackPlayer::class.java)
        i.putExtra("max", maxTime)
        i.putExtra("current", curTime)
        startService(i)
    }

}



