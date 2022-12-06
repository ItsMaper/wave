package com.coffenow.wave.activities

import android.os.Build
import android.os.Bundle
import android.view.View
import android.webkit.WebSettings
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.coffenow.wave.adapter.PlaylistItemAdapter
import com.coffenow.wave.databinding.ActivityPlayerBinding

class PlayerActivity : AppCompatActivity() {

    private var _binding: ActivityPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initRecyclerView()
        val videoImg = intent.getStringExtra("video_img")
        val videoTitle = intent.getStringExtra("video_title")
        val videoChannel= intent.getStringExtra("channelTitle")
        val videoId = intent.getStringExtra("videoId")
        val url = "https://coffenow.pythonanywhere.com/converters/ytdl?id=$videoId&type=stream"

        val playerView:WebView= binding.playerWebView
        playerView.settings.javaScriptEnabled = true
        if (Build.VERSION.SDK_INT >= 21)         {
            playerView.settings.mixedContentMode = WebSettings.MIXED_CONTENT_ALWAYS_ALLOW;          }
        playerView.setLayerType(View.LAYER_TYPE_NONE, null)
        playerView.loadUrl(url)
        playerView.isVerticalScrollBarEnabled = false;
        playerView.isHorizontalScrollBarEnabled = false;


        Glide.with(this).load(videoImg).into(binding.youtubePlayer)
        binding.tvVideoTitle.text = videoTitle
        binding.tvVideoChannel.text = videoChannel


    }

    fun initRecyclerView(){
        val recyclerView = binding.rvPlaylist
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = PlaylistItemAdapter()
    }
}




