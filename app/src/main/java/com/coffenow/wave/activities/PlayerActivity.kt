package com.coffenow.wave.activities

import android.annotation.SuppressLint
import android.os.Bundle
import android.webkit.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.coffenow.wave.adapter.VideoAdapter
import com.coffenow.wave.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private var _binding: ActivityPlayerBinding? = null
    private val binding get() = _binding!!


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setPlayer()
        initRecyclerView()
    }

    @SuppressLint("SetJavaScriptEnabled")
    private fun setPlayer() {
        val videoImg = intent.getStringExtra("video_img")
        val videoTitle = intent.getStringExtra("video_title")
        val videoChannel= intent.getStringExtra("channelTitle")
        val videoId = intent.getStringExtra("videoId")
        val url = "https://coffenow.pythonanywhere.com/converters/ytdl?id=$videoId&type=stream"

        Glide.with(this).load(videoImg).into(binding.youtubePlayer)
        binding.tvVideoTitle.text = videoTitle
        binding.tvVideoChannel.text = videoChannel

        val playerView:WebView= binding.playerWebView
        playerView.isVerticalScrollBarEnabled = false
        playerView.isHorizontalScrollBarEnabled = false
        playerView.webChromeClient = object : WebChromeClient(){}
        playerView.webViewClient=object: WebViewClient(){}

        val settings:WebSettings = playerView.settings
        settings.javaScriptEnabled = true
        settings.domStorageEnabled =true
        playerView.loadUrl(url)
    }

    private fun initRecyclerView(){
        val recyclerView = binding.rvPlaylist
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = VideoAdapter()
        val videoImg = intent.getStringExtra("video_img")
        val videoTitle = intent.getStringExtra("video_title")
        val videoChannel= intent.getStringExtra("channelTitle")
        val videoId = intent.getStringExtra("videoId")
    }
}




