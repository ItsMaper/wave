package com.coffenow.wave.activities

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AbsListView
import android.widget.ImageButton
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.findViewTreeLifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.R
import com.coffenow.wave.activities.viewmodel.PlayerViewModel
import com.coffenow.wave.adapter.PlayerPlaylistAdapter
import com.coffenow.wave.adapter.PlaylistAdapter
import com.coffenow.wave.databinding.ActivityPlayerBinding
import com.coffenow.wave.model.YTModel
import com.coffenow.wave.ui.home.HomeViewModel
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
    private val playListAdapter=PlayerPlaylistAdapter()
    private lateinit var playBtn:ImageButton
    private lateinit var prevBtn:ImageButton
    private lateinit var nextBtn:ImageButton
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar
    private lateinit var timeTotal : TextView
    private lateinit var currentTime : TextView
    private var youTubePlayer:YouTubePlayer? = null
    private var isPlaying =false
    private var maxTime:Float = 0F
    private var curTime:Float = 0F
    private var isLoading = false
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1
    private var isAllVideoLoaded = false

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        playerViewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        return super.onCreateView(name, context, attrs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        val type:String= intent.getStringExtra("type").toString()
        if (type=="local") setLocalPlayer() else if(type == "web") setWebPlayer()
        binding.ytpl.visibility = INVISIBLE
        setterBind()
        initRecyclerView()
        initAdsView()
    }

    private fun initRecyclerView() {
        playerViewModel?.videoId = intent.getStringExtra("id").toString()
        val manager = LinearLayoutManager(this)
        binding.rvPlaylist.apply {
            adapter = playListAdapter
            layoutManager = manager
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                        isScroll = true
                        }}
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    currentItem = manager.childCount
                    totalItem = manager.itemCount
                    scrollOutItem = manager.findFirstVisibleItemPosition()
                    if (isScroll && (currentItem + scrollOutItem == totalItem)){
                        isScroll = false
                        if (!isLoading && totalItem<=23){
                            if (!isAllVideoLoaded){ playerViewModel?.getPlayerlist() } } } } }) }

        }

    private fun setterBind() {
        playBtn = binding.playerPlay
        prevBtn = binding.playerPrev
        nextBtn = binding.playerNext
        seekBar = binding.playerSeek
        timeTotal = binding.time
        currentTime = binding.currentTime
        playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        prevBtn.setImageResource(R.drawable.ic_baseline_arrow_back_ios_24)
        nextBtn.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24)
    }

    private fun initAdsView() {
        MobileAds.initialize(this) {}
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
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
    private fun nextQ(){
        nextBtn.setOnClickListener {
        }
    }
    private fun prevQ(){
        prevBtn.setOnClickListener {
        }
    }
    fun formatTime(t: Int) : String {
        val hours = t / 3600
        val minutes = (t % 3600) / 60
        val seconds = t % 60
        if (hours == 0) {
            if (minutes<=9){
                if (seconds <= 9) {
                    return "0$minutes:0$seconds"
                } else {
                    return "0$minutes:$seconds"
                }
            }else{
                if (seconds <= 9) {
                    return "$minutes:0$seconds"
                } else {
                    return "$minutes:$seconds"
                }}
        } else{
            if (minutes<=9){
                if (seconds <= 9) {
                    return "$hours:0$minutes:0$seconds"
                } else {
                    return "$hours:0$minutes:$seconds"
                }
            }else{
                if (seconds <= 9) {
                    return "$hours:$minutes:0$seconds"
                } else {
                    return "$hours:0$minutes:$seconds"
                }}
        }
        return "$hours:$minutes:$seconds"
    }

    //WEB!
    private fun setWebPlayer() {
        binding.playerPublisher.visibility = VISIBLE
        val thumbnail = intent.getStringExtra("thumbnail")
        val title = intent.getStringExtra("title")
        val publisher = intent.getStringExtra("publisher")
        Glide.with(this).load(thumbnail).into(binding.playerThumbnail)
        binding.playerTitle.text = title
        binding.playerPublisher.text = publisher
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
                youTubePlayer?.seekTo(progress.toFloat())
            }
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {} } )
    }

    override fun onApiChange(youTubePlayer: YouTubePlayer) {}

    override fun onReady(youTubePlayer: YouTubePlayer) {
        val videoId = intent.getStringExtra("id")
        videoId?.let {
            youTubePlayer.loadVideo(it, 0f)
        }
        this.youTubePlayer =youTubePlayer
        setWebSeekBar()
    }
    override fun onError(youTubePlayer: YouTubePlayer, error: PlayerConstants.PlayerError) {}

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        if (state == PlayerConstants.PlayerState.PLAYING ){
            playBtn.setImageResource(R.drawable.ic_baseline_motion_photos_paused_24)
            isPlaying=true
        } else {
            playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            isPlaying=false
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

    override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {}

    override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {}
    override fun onPlaybackQualityChange(youTubePlayer: YouTubePlayer, playbackQuality: PlayerConstants.PlaybackQuality) {}
    override fun onPlaybackRateChange(youTubePlayer: YouTubePlayer, playbackRate: PlayerConstants.PlaybackRate) {}

}



