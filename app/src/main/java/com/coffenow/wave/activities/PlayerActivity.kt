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
import android.widget.AbsListView
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
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
import com.coffenow.wave.model.OnBackPlayerTime
import com.coffenow.wave.services.OnBackPlayer
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import kotlinx.coroutines.*


class PlayerActivity : AppCompatActivity() {

    private var _binding: ActivityPlayerBinding? = null
    private val binding get() = _binding!!
    private var viewModel: PlayerViewModel? =null
    private val playlistAdapter=PlayerPlaylistAdapter()
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db: SQLiteDatabase
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
    private lateinit var PlaylistLayout: ConstraintLayout
    private lateinit var queueText: TextView
    private var playlistName: String? =null
    private var idAutoLoad:String? = null
    private var firstID : String? = null
    private var firstName :String? = null
    private var firstPublisher:String? = null
    private var firstThumbnail:String? = null
    private var isLoading = false
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1

    companion object{
        var isPlaying : MutableLiveData<Boolean> = MutableLiveData(false)
        var playControl = MutableLiveData<Boolean>()
        var duration = MutableLiveData<OnBackPlayerTime>()
        var currentSecond = MutableLiveData<OnBackPlayerTime>()
        var userSecond = MutableLiveData<Float>()
        var playlistService = MutableLiveData<DBModel>()
        var currentQueue : MutableLiveData<Int> = MutableLiveData(1)
    }

    override fun onCreateView(name: String, context: Context, attrs: AttributeSet): View? {
        viewModel = ViewModelProvider(this)[PlayerViewModel::class.java]
        return super.onCreateView(name, context, attrs)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        setBind()
        clickListeners()
        setObserver()
        if (playlistName == "default"){
            initWebPlaylist() } else{initDBPlaylist(playlistName!!)}
        initAdsView()
    }

    private fun startService() {
        Intent(this,OnBackPlayer::class.java).also {
            startService(it)
        }
    }

    private fun setBind() {
        dbHelper = WaveDBHelper(this)
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
        setWebSeekBar()
    }
    private fun clickListeners() {
        val pos = currentQueue.value!!
        favoriteBtn.setOnClickListener {
            val args = arrayOf(idAutoLoad)
            db = dbHelper.writableDatabase
            if (isFavorite()){
                db.delete("favorites", "videoID = ?", args)
                favoriteSetter()
            } else{
                viewModel?.playlistData?.observe(this) {
                    val data = ContentValues()
                    data.put("videoID", it.items[pos].id)
                    data.put("title", it.items[pos].title)
                    data.put("publisher", it.items[pos].channelName)
                    data.put("thumbnail", it.items[pos].thumb)
                    db.insert("favorites",null, data)
            }
                favoriteSetter()
        }}
        playBtn.setOnClickListener {
            playControl.value = isPlaying.value != true
        }

        nextBtn.setOnClickListener {
            webQueueManagement(true) }
        prevBtn.setOnClickListener {
            webQueueManagement(false) }

    }

    private fun setObserver() {
        isPlaying.observe(this){
            if (it){
                playBtn.setImageResource(R.drawable.ic_baseline_motion_photos_paused_24)
            } else{
                playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
            }
        }
        duration.observe(this){
            seekBar.max =it.seekBar
            timeTotal.text = it.time
        }
        currentSecond.observe(this){
            seekBar.progress = it.seekBar
            currentTime.text = it.time
        }

        playlistAdapter.currentSelected.observe(this){
            if (it != currentQueue.value){
                currentQueue.value = it
            }
            if (it != currentQueue.value){
                currentQueue.value = it
            }
        }

        currentQueue.observe(this){
            if (it != playlistAdapter.currentSelected.value){
                playlistAdapter.currentSelected.value = it
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


    private fun initDBPlaylist(playlistName: String){
        favoriteBtn.setBackgroundResource(R.drawable.ic_baseline_favorite)
        db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM $playlistName",null
        )
        viewModel?.parseDBData(cursor)
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
        playlistService = viewModel?.playlistData!!
        playlistAdapter.addListener = PlayerPlaylistAdapter.ItemClickListener { data ->
            idAutoLoad =data.id
            favoriteSetter()
            dbSearchesUpdater(data.id,data.title,data.channelName,data.thumb)
            playerTitle.text = data.title
            playerPublisher.text = data.channelName
            Glide.with(binding.root)
                .load(data.thumb)
                .dontAnimate()
                .dontTransform()
                .into(playerThumbnail)
        }

        viewModel?.playlistData?.observe(this) {
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
        viewModel?.firsItem = DBModel.Items(firstID!!,firstName!!,firstPublisher!!,firstThumbnail!!)
        viewModel?.relatedTo = firstID
        viewModel?.getApiData()

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
                            viewModel?.getApiData() } } } } ) }

        playlistService = viewModel?.playlistData!!
        startService()

        playlistAdapter.addListener = PlayerPlaylistAdapter.ItemClickListener { data ->
            dbSearchesUpdater(data.id,data.title,data.channelName,data.thumb)
            idAutoLoad = data.id
            favoriteSetter()
            playerTitle.text = data.title
            playerPublisher.text = data.channelName
            Glide.with(binding.root)
                .load(data.thumb)
                .dontAnimate()
                .dontTransform()
                .into(playerThumbnail)
        }

        viewModel?.playlistData?.observe(this) {
            playlistAdapter.setDataDiff(it.items, rvPlaylist)
            idAutoLoad = it.items[0].id
            favoriteSetter()
            playerTitle.text = it.items[0].title
            playerPublisher.text = it.items[0].channelName
            Glide.with(this)
                .load(it.items[0].thumb)
                .into(playerThumbnail)}
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

    private fun webQueueManagement(isNext : Boolean) {
        val pos =currentQueue.value!!
        isPlaying.value = false
        seekBar.progress=0
        currentTime.text= "00:00"
        viewModel?.playlistData?.observe(this) {
            if (isNext){
                if(pos >=0 && pos != playlistAdapter.itemCount){
                    currentQueue.value = pos +1
                } else{
                    currentQueue.value = 0
                }
                playlistAdapter.currentSelected.value = currentQueue.value } else {
                if (pos > 0) {
                    currentQueue.value = pos - 1 }
                playlistAdapter.currentSelected.value = currentQueue.value  }

            idAutoLoad = it.items[currentQueue.value!!].id
            favoriteSetter()
            dbSearchesUpdater(it?.items?.get(currentQueue.value!!)?.id,it?.items?.get(currentQueue.value!!)?.title,it?.items?.get(currentQueue.value!!)?.channelName,it?.items?.get(currentQueue.value!!)?.thumb)
            playerTitle.text = it?.items?.get(currentQueue.value!!)?.title
            playerPublisher.text = it?.items?.get(currentQueue.value!!)?.channelName
            Glide.with(binding.root)
                .load(it?.items?.get(currentQueue.value!!)?.thumb)
                .dontAnimate()
                .dontTransform()
                .into(playerThumbnail)
            }
    }

    private fun setWebSeekBar(){
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    userSecond.value = progress.toFloat()
                }else{
                    if (seekBar?.progress  == seekBar?.max){
                        webQueueManagement(true) } }}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {} } )
    }
}



