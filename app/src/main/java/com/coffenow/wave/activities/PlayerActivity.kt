package com.coffenow.wave.activities

import android.os.Bundle
import android.webkit.WebView
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide

import com.coffenow.wave.databinding.ActivityPlayerBinding


class PlayerActivity : AppCompatActivity() {

    private var _binding: ActivityPlayerBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val videoImg = intent.getStringExtra("video_img")
        val videoTitle = intent.getStringExtra("video_title")
        val videoChannel= intent.getStringExtra("channelTitle")
        val videoId = intent.getStringExtra("videoId")
        print(videoId)
        val baseURL = "http://192.168.1.4:5000/converters/ytdl?id=$videoId&type=stream"

        val playerView: WebView = binding.playerWebView
        playerView.settings.javaScriptEnabled = true
        playerView.loadUrl(baseURL)

        Glide.with(this).load(videoImg).into(binding.youtubePlayer)
        binding.tvVideoTitle.text = videoTitle
        binding.tvVideoChannel.text = videoChannel
    }
}




