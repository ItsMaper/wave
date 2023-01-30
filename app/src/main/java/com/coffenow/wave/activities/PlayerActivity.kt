package com.coffenow.wave.activities

import android.annotation.SuppressLint
import android.app.*
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.palette.graphics.Palette
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DataSource
import com.bumptech.glide.load.engine.GlideException
import com.bumptech.glide.request.RequestListener
import com.bumptech.glide.request.target.Target
import com.coffenow.wave.R
import com.coffenow.wave.activities.viewmodel.PlayerViewModel
import com.coffenow.wave.adapter.PlayerPlaylistAdapter
import com.coffenow.wave.databinding.ActivityPlayerBinding
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.OnBackPlayerTime
import com.coffenow.wave.services.OnBackPlayer
import com.coffenow.wave.services.OnBackPlayer.Companion.currentQueue
import com.coffenow.wave.services.OnBackPlayer.Companion.fromNotifReturn
import com.coffenow.wave.services.OnBackPlayer.Companion.isBucle
import com.coffenow.wave.services.OnBackPlayer.Companion.isLive
import com.coffenow.wave.services.OnBackPlayer.Companion.isPlaying
import com.coffenow.wave.services.OnBackPlayer.Companion.isShuffled
import com.coffenow.wave.services.OnBackPlayer.Companion.playControl
import com.coffenow.wave.services.OnBackPlayer.Companion.player
import com.coffenow.wave.services.OnBackPlayer.Companion.playlistService
import com.coffenow.wave.utils.WaveDBHelper
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
    private lateinit var addToBtn : ImageButton
    private lateinit var seekBar: SeekBar
    private lateinit var timeTotal : TextView
    private lateinit var currentTime : TextView
    private lateinit var title:TextView
    private lateinit var queueText: TextView

    private var playlistName: String? =null
    private var playlistFromQuery: String? =null
    private var idAutoLoad:String? = null
    private var firstID : String? = null
    private var firstName :String? = null
    private var firstPublisher:String? = null
    private var firstThumbnail:String? = null
    private var firstState: String? = null
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1

    private val glideCall = object : RequestListener<Drawable>{
        override fun onLoadFailed(e: GlideException?, model: Any?, target: Target<Drawable>?, isFirstResource: Boolean): Boolean {
            return false
        }

        override fun onResourceReady(resource: Drawable?, model: Any?, target: Target<Drawable>?, dataSource: DataSource?, isFirstResource: Boolean): Boolean {
            val bitmap = resource!!.toBitmap()
            var paletteGlide : Palette? = null
            Palette.from(bitmap).generate {
                it?.let { paletteGlide= it }
            }
            if (paletteGlide != null){
                setVibrantPalette(paletteGlide!!)
            }
            return false
        }

    }

    companion object{
        var duration = MutableLiveData<OnBackPlayerTime>()
        var currentSecond = MutableLiveData<OnBackPlayerTime>()
        var userSecond = MutableLiveData<Float>()
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
        if (playlistName == null && player != null){
            fromNotifReturn = true
            viewModel?.playlistData = playlistService!!
        } else{
            if(playlistFromQuery!=null){
                title.text = playlistFromQuery
                getViewModel(false)
            } else{
                getViewModel(true)
                }
        }
        initRecyclerView()
        initAdsView()
    }

    private fun setBind() {
        dbHelper = WaveDBHelper(this)
        title = binding.playerPlaylistTitle
        rvPlaylist = binding.rvPPlaylist
        playerTitle = binding.titleTV
        queueText = binding.queueText
        playerPublisher = binding.publisherTV
        playerThumbnail = binding.thumbIV
        playBtn = binding.playBTN
        prevBtn = binding.previousBTN
        nextBtn = binding.nextBTN
        bucleBtn=binding.bucleBTN
        shuffleBtn= binding.shuffleBTN
        favoriteBtn = binding.favoriteBTN
        addToBtn = binding.addToPlaylistBtn
        seekBar = binding.playerSeek
        timeTotal = binding.totalTimeTV
        currentTime = binding.currentTimeTV
        playBtn.setImageResource(R.drawable.icon_player_play)
        prevBtn.setImageResource(R.drawable.icon_player_back)
        nextBtn.setImageResource(R.drawable.icon_player_next)
        bucleBtn.setImageResource(R.drawable.icon_player_bucle)
        shuffleBtn.setImageResource(R.drawable.icon_player_shuffle)
        addToBtn.setImageResource(R.drawable.icon_add_to_playlist)
        shuffleBtn.alpha = 0.70f
        bucleBtn.alpha = 0.70f
        firstID = intent.getStringExtra("id")
        firstName = intent.getStringExtra("title")
        firstPublisher = intent.getStringExtra("publisher")
        firstThumbnail = intent.getStringExtra("thumbnail")
        firstState = intent.getStringExtra("state")
        playlistName = intent.getStringExtra("playlist")
        playlistFromQuery = intent.getStringExtra("query")
        setWebSeekBar()
    }

    private fun onBackPressedC() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addCategory(Intent.CATEGORY_HOME)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
        finish()
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
                    if (it.items[pos].live != "live"){
                        val data = ContentValues()
                        data.put("videoID", it.items[pos].id)
                        data.put("title", it.items[pos].title)
                        data.put("publisher", it.items[pos].channelName)
                        data.put("thumbnail", it.items[pos].thumb)
                        db.insert("favorites",null, data)
                    } else{
                        Toast.makeText(this,"You cannot save live videos", Toast.LENGTH_SHORT)
                    }
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

        shuffleBtn.setOnClickListener {
            isShuffled.value = isShuffled.value != true
        }

        bucleBtn.setOnClickListener {
            isBucle.value = isBucle.value != true
        }
        addToBtn.setOnClickListener {
            val fragment : androidx.fragment.app.DialogFragment = com.coffenow.wave.ui.popUps.AddToPlaylistFragment(this)
            fragment.show(supportFragmentManager, "Select")
        }
        binding.IbackBTN.setOnClickListener {
            onBackPressedC()
        }
    }
        private fun setVibrantPalette(paletteGlide: Palette){
            val swatchDarkVibrant = paletteGlide.darkVibrantSwatch
            if(swatchDarkVibrant!= null){
                binding.playerContainer.setBackgroundColor(swatchDarkVibrant.rgb)
            }
        }


    private fun setObserver() {
        isPlaying.observe(this){
            if (it){
                playBtn.setImageResource(R.drawable.icon_player_paused) } else{
                playBtn.setImageResource(R.drawable.icon_player_play) } }

        isShuffled.observe(this){
            if (it){
                shuffleBtn.alpha = 1f }else{
                shuffleBtn.alpha = 0.70f } }

        isBucle.observe(this){
            if (it){
                bucleBtn.alpha = 1f }else{
                bucleBtn.alpha = 0.70f } }

        duration.observe(this){
            isLive.value?.let { live->
                if (!live){
                    if(it.time == 0){
                        currentTime.visibility = VISIBLE
                        currentTime.text = resources.getText(R.string.timer)
                        timeTotal.text = resources.getText(R.string.timer)}else{
                        seekBar.max =it.time
                        timeTotal.text = formatTime(it.time) }
                } else{
                    timeTotal.text = "Live"
                    seekBar.progress = seekBar.max
                    currentTime.visibility = INVISIBLE
                }
            } }

        currentSecond.observe(this){
            isLive.value?.let {live->
                if (!live){
                    if(it.time == 0){
                        currentTime.text = resources.getText(R.string.timer)
                        timeTotal.text = resources.getText(R.string.timer)
                        seekBar.progress = 0 }else{
                        seekBar.progress = it.time
                        currentTime.text = formatTime(it.time)}
                } else{
                    currentTime.visibility = INVISIBLE
                }
            } }

        currentQueue.observe(this){
            playlistService?.observe(this) { data ->
                idAutoLoad = data.items[it].id
                favoriteSetter()
                playerTitle.text = data.items[it].title
                playerPublisher.text = data.items[it].channelName
                Glide.with(this)
                    .load(data.items[it].thumb)
                    .centerCrop()
                    .listener(glideCall)
                    .into(playerThumbnail) }
            }

    }

    private fun getViewModel(fromPlaylist: Boolean){
        seekBar.progress = 0
        db = dbHelper.readableDatabase

        if (fromPlaylist){
            if (playlistName == "default"){
                viewModel?.first = true
                viewModel?.firsItem = DBModel.Items(firstID!!,firstName!!,firstPublisher!!,firstThumbnail!!, firstState!!)
                viewModel?.relatedTo = firstID
                viewModel?.getApiData() }else{
                viewModel?.first = false
                title.text = playlistName
                val cursor: Cursor = db.rawQuery("SELECT * FROM $playlistName", null)
                if (cursor.moveToFirst()){
                    val rand: Int = (0 until (cursor.count-1)).random()
                    cursor.move(rand)
                    viewModel?.relatedTo = cursor.getString(0)
                    viewModel?.parseDBData(cursor)
                    cursor.close() } }
        } else{
            viewModel?.first = false
            viewModel?.querySearch = playlistFromQuery
            viewModel?.getApiDataQuery()
        }
        playlistService = viewModel?.playlistData!!
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
                    /*if (isScroll && (currentItem + scrollOutItem == totalItem)){
                        isScroll = false
                        if (!isLoading && totalItem<=20){
                            viewModel?.getApiData() } }*/ } } ) }

        playlistAdapter.addListener = PlayerPlaylistAdapter.ItemClickListener { data ->
            idAutoLoad = data.id
            currentQueue.value = playlistAdapter.currentSelected.value
            favoriteSetter()}

        playlistService?.observe(this) {
            playlistAdapter.clearAll()
            playlistAdapter.setDataDiff(it.items, rvPlaylist)
            idAutoLoad = it.items[0].id
            favoriteSetter() }
    }

    private fun webQueueManagement(isNext : Boolean) {
        isPlaying.value = false
        seekBar.progress=0
        if (isNext){
            currentQueue.value?.let { it1 ->
                if (it1 < PlayerPlaylistAdapter.itemsSize.value!!-1){
                    currentQueue.value = it1+1 }else{
                    isBucle.value?.let {
                        if(it){
                            currentQueue.value = 0 } } } } } else {
                currentQueue.value?.let {
                    if (it > 0 ){
                        currentQueue.value = it - 1 } } }

        playlistService?.observe(this) { data ->
            currentQueue.observe(this){
                idAutoLoad = data.items[it].id
                favoriteSetter()
                playerTitle.text = data.items[it].title
                playerPublisher.text = data.items[it].channelName
                Glide.with(this)
                    .load(data.items[it].thumb)
                    .centerCrop()
                    .listener(glideCall)
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
            favoriteBtn.setBackgroundResource(R.drawable.icon_player_favorite)
        } else {
            favoriteBtn.setBackgroundResource(R.drawable.icon_player_favorite_border)
        }
    }
    private fun isFavorite(): Boolean{
        db = dbHelper.readableDatabase
        val sql = "SELECT * FROM favorites WHERE videoID LIKE('$idAutoLoad')"
        val cursor = db.rawQuery(sql, null)
        return cursor.moveToFirst()
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

    override fun onStop() {
        super.onStop()
        playlistService?.removeObservers(this)
        currentQueue.removeObservers(this)
        currentSecond.removeObservers(this)
    }
}



