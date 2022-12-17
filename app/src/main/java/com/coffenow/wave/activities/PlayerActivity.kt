package com.coffenow.wave.activities

import android.content.Context
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.Button
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.coffenow.wave.R
import com.coffenow.wave.databinding.ActivityPlayerBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds
import java.util.concurrent.TimeUnit


class PlayerActivity : AppCompatActivity() {

    private var _binding: ActivityPlayerBinding? = null
    private val binding get() = _binding!!
    private lateinit var playBtn:Button
    private lateinit var prevBtn:Button
    private lateinit var nextBtn:Button
    private lateinit var mediaPlayer: MediaPlayer
    private lateinit var seekBar: SeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        _binding = ActivityPlayerBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        initAdsView()


        val type:String= intent.getStringExtra("type").toString()
        if (type=="local") setLocalPlayer() else if(type == "web") setWebPlayer()
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
        binding.playerTitle.text = title
    }

    private fun setWebPlayer() {
        binding.playerPublisher.visibility = VISIBLE
        val thumbnail = intent.getStringExtra("thumbnail")
        val title = intent.getStringExtra("title")
        val publisher = intent.getStringExtra("publisher")
        val id = intent.getStringExtra("id")
        Glide.with(this).load(thumbnail).into(binding.playerThumbnail)
        binding.playerTitle.text = title
        binding.playerPublisher.text = publisher
        val url = "https://www.youtube.com/watch?v=$id"
        mediaPlayer(url)
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
            setSeekBar()
        } catch (e: Exception){
            e.printStackTrace()
        }
    }


    private fun setSeekBar() {
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
            mediaPlayer.release()
        }
    }
    private fun prevQ(){
        prevBtn.setOnClickListener {
            mediaPlayer
        }
    }
    fun formatDuration(duration: Long):String{
        val minutes = TimeUnit.MINUTES.convert(duration, TimeUnit.MILLISECONDS)
        val seconds = (TimeUnit.SECONDS.convert(duration, TimeUnit.MILLISECONDS) -
                minutes* TimeUnit.SECONDS.convert(1, TimeUnit.MINUTES))
        return String.format("%02d:%02d", minutes, seconds)
    }
}



