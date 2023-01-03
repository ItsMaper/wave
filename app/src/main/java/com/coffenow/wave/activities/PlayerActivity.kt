package com.coffenow.wave.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AbsListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.NotificationCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.R
import com.coffenow.wave.activities.viewmodel.PlayerViewModel
import com.coffenow.wave.adapter.PlayerPlaylistAdapter
import com.coffenow.wave.databinding.ActivityPlayerBinding
import com.coffenow.wave.db.WaveDBHelper
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.services.OnBackPlayer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.PlayerConstants
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.YouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.options.IFramePlayerOptions
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import kotlinx.coroutines.*


class PlayerActivity : AppCompatActivity(), YouTubePlayerListener {

    private var _binding: ActivityPlayerBinding? = null
    private val binding get() = _binding!!
    private var playerViewModel:PlayerViewModel?=null
    private var backPlayer : OnBackPlayer? = null
    private val playlistAdapter=PlayerPlaylistAdapter()
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var ytpl: YouTubePlayerView
    private lateinit var rvPlaylist: RecyclerView
    private lateinit var playerTitle: TextView
    private lateinit var playerPublisher: TextView
    private lateinit var playerThumbnail: ImageView
    private lateinit var playBtn:ImageButton
    private lateinit var prevBtn:ImageButton
    private lateinit var nextBtn:ImageButton
    private lateinit var bucleBtn:ImageButton
    private lateinit var shuffleBtn:ImageButton
    private lateinit var favoriteBtn: ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var timeTotal : TextView
    private lateinit var currentTime : TextView
    private lateinit var notificationStyle: Notification
    private lateinit var PlaylistLayout: ConstraintLayout
    private lateinit var queueText: TextView
    private var playlistName: String? =null
    private var idAutoLoad:String? = null
    private var firstID : String? = null
    private var firstName :String? = null
    private var firstPublisher:String? = null
    private var firstThumbnail:String? = null
    private val notificationStyleID = 0
    private val channelName = "channelName"
    private val channelId = "channelId"
    private var youtubePlayer:YouTubePlayer? = null
    private var isPlaying =false
    private var maxTime:Float = 0F
    private var curTime:Int = 0
    private var isLoading = false
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1
    private var isPlaylistFullScreen = false
    private var fromAutoPlay = false

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
        setWebPlayer()
        if (playlistName == "default"){
            initWebPlaylist() } else{initDBPlaylist(playlistName!!)}
        clickListeners()
        //playerNotification()
        //val notifManager = NotificationManagerCompat.from(this)
        //notifManager.notify(notificationStyleID, notificationStyle)
        initAdsView()
    }

    private fun setterBind() {
        backPlayer = OnBackPlayer()
        dbHelper = WaveDBHelper(this)
        ytpl = binding.ytpl
        ytpl.visibility = INVISIBLE
        rvPlaylist = binding.rvPPlaylist
        playerTitle = binding.playerTitle
        playerPublisher = binding.playerPublisher
        queueText = binding.queueText
        playerThumbnail= binding.playerThumbnail
        playBtn = binding.playerPlay
        prevBtn = binding.playerPrev
        nextBtn = binding.playerNext
        bucleBtn=binding.playerBucle
        shuffleBtn=binding.playerShuffle
        favoriteBtn = binding.favoriteButton
        seekBar = binding.playerSeek
        timeTotal = binding.timeText
        currentTime = binding.currentTimeText
        PlaylistLayout = binding.PPlaylistLayout
        playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        prevBtn.setImageResource(R.drawable.ic_baseline_arrow_back_ios_24)
        nextBtn.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24)
        firstID = intent.getStringExtra("id")
        firstName = intent.getStringExtra("title")
        firstPublisher = intent.getStringExtra("publisher")
        firstThumbnail = intent.getStringExtra("thumbnail")
        playlistName = intent.getStringExtra("playlist")
    }
    private fun clickListeners() {
        favoriteBtn.setOnClickListener {
            val args = arrayOf(idAutoLoad)
            db = dbHelper.writableDatabase
            if (isFavorite()){
                db.delete("favorites", "videoID = ?", args)
                favoriteSetter()
            } else{
                val currentQueue = playlistAdapter.currentSelected
                playerViewModel?.playlistData?.observe(this) {
                    val data = ContentValues()
                    data.put("videoID", it.items[currentQueue!!].id)
                    data.put("title", it.items[currentQueue].title)
                    data.put("publisher", it.items[currentQueue].channelName)
                    data.put("thumbnail", it.items[currentQueue].thumb)
                    db.insert("favorites",null, data)
            }
                favoriteSetter()
        }}

        nextBtn.setOnClickListener {
            webQueueManagement(true) }
        prevBtn.setOnClickListener {
            webQueueManagement(false) }

        PlaylistLayout.setOnClickListener {
            if (isPlaylistFullScreen){
                it.translationY = 0F
                isPlaylistFullScreen = false
                bucleBtn.visibility = VISIBLE
                shuffleBtn.visibility = VISIBLE
                queueText.text = "$totalItem Songs"
            }else{
                it.translationY = -(binding.root.height-(playerThumbnail.height*1.8)).toFloat()
                isPlaylistFullScreen = true
                bucleBtn.visibility = INVISIBLE
                shuffleBtn.visibility = INVISIBLE
                queueText.text = binding.playerTitle.text
            }
        }
    }

    private fun favoriteSetter() {
        if (isFavorite()){
            favoriteBtn.setBackgroundResource(R.drawable.ic_baseline_favorite)
        } else {
            favoriteBtn.setBackgroundResource(R.drawable.ic_baseline_favorite_border)
        }
    }

    private fun isFavorite(): Boolean{
        db = dbHelper.readableDatabase
        val sql = "SELECT * FROM favorites WHERE videoID LIKE('$idAutoLoad')"
        val cursor = db.rawQuery(sql, null)
        return cursor.moveToFirst()
        }

    private fun initAdsView() {
        MobileAds.initialize(this) {}
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
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


    private fun initDBPlaylist(playlistName: String){
        favoriteBtn.setBackgroundResource(R.drawable.ic_baseline_favorite)
        db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $playlistName",null
        )
        playerViewModel?.parseDBData(cursor)
        val manager = LinearLayoutManager(this)
        rvPlaylist.apply {
            layoutManager = manager
            adapter = playlistAdapter
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
                        } } } )
        }
        queueText.text = cursor.count.toString()
        playlistAdapter.addListener = PlayerPlaylistAdapter.ItemClickListener { data ->
            youtubePlayer?.loadVideo(data.id, 0f)
            idAutoLoad =data.id
            favoriteSetter()
            dbSearchesUpdater(data.id,data.title,data.channelName,data.thumb)
            playerTitle.text = data.title
            playerPublisher.text = data.channelName
            queueText.text = data.title
            Glide.with(binding.root)
                .load(data.thumb)
                .dontAnimate()
                .dontTransform()
                .into(playerThumbnail)
        }

        playerViewModel?.playlistData?.observe(this) {
            playlistAdapter.setDataDiff(it.items, rvPlaylist)
            idAutoLoad = it.items[0].id
            favoriteSetter()
            playerTitle.text = it.items[0].title
            playerPublisher.text = it.items[0].channelName
            Glide.with(this)
                .load(it.items[0].thumb)
                .into(playerThumbnail)}
    }


    @SuppressLint("SetTextI18n")
    private fun initWebPlaylist() {
        playerViewModel?.firsItem = DBModel.Items(firstID!!,firstName!!,firstPublisher!!,firstThumbnail!!)
        playerViewModel?.relatedTo = firstID
        playerViewModel?.getApiData()

        val manager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        rvPlaylist.apply {
            adapter = playlistAdapter
            layoutManager = manager
            binding.queueText.text="Songs"
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
                        if (!isLoading && totalItem<=12){
                            playerViewModel?.getApiData() } } } } ) }

        playlistAdapter.addListener = PlayerPlaylistAdapter.ItemClickListener { data ->
            data.id.let { id -> id.let { youtubePlayer?.loadVideo(it, 0f) } }
            dbSearchesUpdater(data.id,data.title,data.channelName,data.thumb)
            idAutoLoad = data.id
            favoriteSetter()
            playerTitle.text = data.title
            playerPublisher.text = data.channelName
            queueText.text = data.title
            Glide.with(binding.root)
                .load(data.thumb)
                .dontAnimate()
                .dontTransform()
                .into(playerThumbnail)
        }

        playerViewModel?.playlistData?.observe(this) {
            playlistAdapter.setDataDiff(it.items, rvPlaylist)
            idAutoLoad = it.items[0].id
            favoriteSetter()
            playerTitle.text = it.items[0].title
            playerPublisher.text = it.items[0].channelName
            Glide.with(this)
                .load(it.items[0].thumb)
                .into(playerThumbnail)}
    }



    //WEB!
    private fun setWebPlayer() {
        playerPublisher.visibility = VISIBLE
        val opts = IFramePlayerOptions.Builder()
            .controls(0)
            .rel(0)
            .ivLoadPolicy(3)
            .ccLoadPolicy(3)
            .build()
        ytpl.initialize(this, false, opts)
        ytpl.enableBackgroundPlayback(true)
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
        seekBar.progress=0
        currentTime.text= "00:00"
        playerViewModel?.playlistData?.observe(this) {
            if (currentQueue != null) {
                if (isNext){
                    if(currentQueue>=0 && currentQueue != playlistAdapter.itemCount){
                        currentQueue+=1
                    }
                    playlistAdapter.currentSelected = currentQueue } else{
                    if (currentQueue >0) {
                        currentQueue-=1
                    }
                    playlistAdapter.currentSelected = currentQueue }
                    it.items[currentQueue].id.let { it1 ->
                        youtubePlayer?.loadVideo(it1, 0f)
                    idAutoLoad = it1}
                favoriteSetter()
                    dbSearchesUpdater(it?.items?.get(currentQueue)?.id,it?.items?.get(currentQueue)?.title,it?.items?.get(currentQueue)?.channelName,it?.items?.get(currentQueue)?.thumb)
                    playerTitle.text = it?.items?.get(currentQueue)?.title
                    playerPublisher.text = it?.items?.get(currentQueue)?.channelName
                    Glide.with(binding.root)
                        .load(it?.items?.get(currentQueue)?.thumb)
                        .dontAnimate()
                        .dontTransform()
                        .into(playerThumbnail) }
                if (isPlaylistFullScreen){
                    queueText.text=playerTitle.text
                }
            }
    }


    override fun onReady(youTubePlayer: YouTubePlayer) {
        youTubePlayer.loadVideo(idAutoLoad!!, 0f)
        this.youtubePlayer =youTubePlayer
        setWebSeekBar()
    }

    override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
        curTime=second.toInt()
        seekBar.progress = curTime
        currentTime.text = playerViewModel?.formatTime(curTime)
    }

    override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
        maxTime = duration
        seekBar.max= maxTime.toInt()
        timeTotal.text= playerViewModel?.formatTime(duration.toInt())
    }

    override fun onStateChange(youTubePlayer: YouTubePlayer, state: PlayerConstants.PlayerState) {
        isPlaying = if (state == PlayerConstants.PlayerState.PLAYING ){
            playBtn.setImageResource(R.drawable.ic_baseline_motion_photos_paused_24)
            true
        } else {
            playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            if(fromAutoPlay){
                youTubePlayer.play()
            }
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

    private fun onBackground(): Boolean {
        while (true){
            if (seekBar.progress+1 >= seekBar.max){
                return true
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fromAutoPlay=false
    }
    override fun onStop() {
        super.onStop()
        fromAutoPlay=true
        GlobalScope.launch {
            if (withContext(Dispatchers.Default){onBackground()}){
                withContext(Dispatchers.Main){webQueueManagement(true)}
                } }
    }

    override fun onDestroy() {
        super.onDestroy()
        ytpl.release()
    }
}



