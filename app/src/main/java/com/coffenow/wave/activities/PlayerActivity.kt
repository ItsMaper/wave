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
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.R
import com.coffenow.wave.activities.viewmodel.PlayerViewModel
import com.coffenow.wave.adapter.AddToPlaylist
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
    private lateinit var addToLayout : FrameLayout
    private lateinit var playerTitle: TextView
    private lateinit var playerPublisher: TextView
    private lateinit var playerThumbnail: ImageView
    private lateinit var playBtn:ImageButton
    private lateinit var prevBtn:ImageButton
    private lateinit var nextBtn:ImageButton
    private lateinit var bucleBtn:ImageButton
    private lateinit var shuffleBtn:ImageButton
    private lateinit var favoriteBtn: ImageButton
    private lateinit var addToBtn: Button
    private lateinit var spinnerOptions: Spinner
    private lateinit var seekBar: SeekBar
    private lateinit var timeTotal : TextView
    private lateinit var currentTime : TextView
    private lateinit var playlistLayout: ConstraintLayout
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
    private var fromUser = false


    companion object{
        var isPlaying : MutableLiveData<Boolean> = MutableLiveData(false)
        var playControl = MutableLiveData<Boolean>()
        var duration = MutableLiveData<OnBackPlayerTime>()
        var currentSecond = MutableLiveData<OnBackPlayerTime>()
        var userSecond = MutableLiveData<Float>()
        var playlistService : MutableLiveData<DBModel>?= null
        var currentQueue = MutableLiveData<Int>()
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
        initService()
        setBind()
        clickListeners()
        setObserver()
        if (playlistService?.value?.items != null){
            viewModel?.playlistData = playlistService!!
        } else{
            getViewModel(playlistName!!)
        }
        initRecyclerView()
        initAdsView()
    }

    private fun setBind() {
        dbHelper = WaveDBHelper(this)
        rvPlaylist = binding.rvPPlaylist
        addToLayout = binding.addToPlaylistPanel
        addToLayout.visibility = INVISIBLE
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
        addToBtn = binding.addBtn
        spinnerOptions = binding.playerSpinnerOptions
        seekBar = binding.playerSeek
        timeTotal = binding.timeText
        currentTime = binding.currentTimeText
        playlistLayout = binding.PPlaylistLayout
        playBtn.setImageResource(R.drawable.ic_baseline_play_circle_outline_24)
        prevBtn.setImageResource(R.drawable.ic_baseline_arrow_back_ios_24)
        nextBtn.setImageResource(R.drawable.ic_baseline_arrow_forward_ios_24)
        firstID = intent.getStringExtra("id")
        firstName = intent.getStringExtra("title")
        firstPublisher = intent.getStringExtra("publisher")
        firstThumbnail = intent.getStringExtra("thumbnail")
        playlistName = intent.getStringExtra("playlist")
        setSpinner()
        setAddPlaylist()
        setWebSeekBar()
    }

    private fun clickListeners() {
        favoriteBtn.setOnClickListener {
            val pos = currentQueue.value!!
            val args = arrayOf(idAutoLoad)
            db = dbHelper.writableDatabase
            if (isFavorite()){
                db.delete("favorites", "videoID = ?", args)
                favoriteSetter()
            } else{
                playlistService?.observe(this) {
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

        addToBtn.setOnClickListener {
            addToLayout.visibility = INVISIBLE
            fromUser = false
        }

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
            seekBar.max =it.time
            timeTotal.text = formatTime(it.time)
        }
        currentSecond.observe(this){
            seekBar.progress = it.time
            currentTime.text = formatTime(it.time)
        }
        currentQueue.observe(this){
            playlistService?.observe(this) { data ->
                idAutoLoad = data.items[it].id
                favoriteSetter()
                playerTitle.text = data.items[it].title
                playerPublisher.text = data.items[it].channelName
                Glide.with(this)
                    .load(data.items[it].thumb)
                    .into(playerThumbnail)
            }
        }
    }

    private fun getViewModel(PlaylistName: String){
        if (PlaylistName == "searches" || PlaylistName == "default"){
            viewModel?.firsItem = DBModel.Items(firstID!!,firstName!!,firstPublisher!!,firstThumbnail!!)
        }
        if (PlaylistName != "default"){
            db = dbHelper.readableDatabase
            val cursor: Cursor = db.rawQuery("SELECT * FROM $playlistName", null)
            if(PlaylistName == "searches"){
                viewModel?.first = true }
            viewModel?.parseDBData(cursor)
            cursor.moveToFirst()
            viewModel?.relatedTo = cursor.getString(0)
        }  else{
            viewModel?.relatedTo = firstID}

        viewModel?.getApiData()
        playlistService = viewModel?.playlistData!!
        OnBackPlayer.playlist = playlistService!!
        currentQueue.value = 0
    }

    @SuppressLint("SetTextI18n")
    private fun initRecyclerView() {
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



        playlistAdapter.addListener = PlayerPlaylistAdapter.ItemClickListener { data ->
            dbSearchesUpdater(data.id,data.title,data.channelName,data.thumb)
            idAutoLoad = data.id
            currentQueue.value = playlistAdapter.currentSelected.value
            favoriteSetter()
        }

        playlistService?.observe(this) {
            playlistAdapter.setDataDiff(it.items, rvPlaylist)
            idAutoLoad = it.items[0].id
            favoriteSetter() }
    }

    private fun setAddPlaylist() {
        val rv= binding.lvPlaylists
        val db = dbHelper.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM playlists",null)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = AddToPlaylist(cursor)
    }

    private fun setSpinner() {
        val data : Array<String> = resources.getStringArray(R.array.MusicSpinnerOptions)
        val spinnerAdapter: ArrayAdapter<*> =
            object : ArrayAdapter<String?>(this, android.R.layout.simple_spinner_item, data) {
                override fun getDropDownView(
                    position: Int,
                    convertView: View?,
                    parent: ViewGroup
                ): View? {
                    fromUser  = true
                    return super.getDropDownView(position+1, convertView, parent)
                }

                override fun getCount(): Int {
                    return data.size - 1
                }
            }
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)

        spinnerOptions.adapter = spinnerAdapter
        spinnerOptions.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (position == 0 && fromUser){
                    addToLayout.visibility = VISIBLE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
            }
        }
    }

    private fun webQueueManagement(isNext : Boolean) {
        isPlaying.value = false
        seekBar.progress=0
        currentTime.text= resources.getText(R.string.timer)
        if (isNext){
            currentQueue.value?.let {
                if (it < playlistAdapter.itemCount-1){
                    currentQueue.value = it+1
                }else{
                    currentQueue.value = 0
                }
                timeTotal.text = resources.getText(R.string.timer)
                currentTime.text = resources.getText(R.string.timer)
            } } else {
                currentQueue.value?.let {
                    if (it > 0 ){
                        currentQueue.value = it - 1
                        timeTotal.text = resources.getText(R.string.timer)
                        currentTime.text = resources.getText(R.string.timer)
                    } }

            }
        playlistService?.observe(this) { data ->
            currentQueue.observe(this){
                idAutoLoad = data.items[it].id
                favoriteSetter()
                playerTitle.text = data.items[it].title
                playerPublisher.text = data.items[it].channelName
                Glide.with(this)
                    .load(data.items[it].thumb)
                    .into(playerThumbnail)}
        }
    }

    private fun setWebSeekBar(){
        seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener{
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if(fromUser){
                    userSecond.value = progress.toFloat()
                    currentTime.text = formatTime(progress)
                }}
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {} } )
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

    private fun initService() {
        Intent(this, OnBackPlayer::class.java).also {
            startService(it)
        }
    }

    private fun initAdsView() {
        MobileAds.initialize(this) {}
        val adView = binding.adView
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
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



