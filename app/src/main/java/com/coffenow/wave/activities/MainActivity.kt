package com.coffenow.wave.activities

import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.coffenow.wave.R
import com.coffenow.wave.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()
        appBar()
        setBackground()
    }

    override fun onBackPressed() {
        finish()
    }

    private fun appBar() {
        val appBarConfiguration = AppBarConfiguration(
            setOf(R.id.navigation_home, R.id.navigation_user, R.id.navigation_library))
        val navView: BottomNavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_activity_main)
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)
    }

    private fun setBackground(){
        val layout = binding.mainContainer
        val animation : AnimationDrawable = layout.background as AnimationDrawable
        animation.setEnterFadeDuration(5000)
        animation.setExitFadeDuration(8000)
        animation.start()

    }



}

