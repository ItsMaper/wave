package com.coffenow.wave.ui.library

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.*
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.R
import com.coffenow.wave.adapter.RecyclerPlaylistByDB
import com.coffenow.wave.databinding.FragmentLibraryBinding
import com.coffenow.wave.db.WaveDBHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds


class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private var playlistViewModel: LibraryViewModel? = null
    private lateinit var appContext : Context
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var rvPlaylists: RecyclerView
    private lateinit var createPanel: FrameLayout
    private lateinit var saveBtn :Button
    private lateinit var inputEditText: EditText
    private lateinit var addBtn: ImageButton


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        playlistViewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBinding()
        initRecyclerView()
        createList()
        initAdsView()
         }

    private fun setBinding(){
        appContext= requireContext().applicationContext
        createPanel = binding.newPlaylistPanel
        createPanel.visibility = INVISIBLE
        inputEditText = binding.playlistName
        inputEditText.hint = "Name"
        dbHelper = WaveDBHelper(appContext)
        rvPlaylists = binding.rvPlaylist
        addBtn= binding.newList
        saveBtn = binding.saveBtn
    }

    private fun createList() {
        db = dbHelper.readableDatabase
        addBtn.setOnClickListener{
            addBtn.visibility = INVISIBLE
            createPanel.visibility = VISIBLE
        }
        saveBtn.setOnClickListener{
            createPanel.visibility = INVISIBLE
            if(inputEditText.text.isNotEmpty()){
                val playlistName: String = inputEditText.text.toString()
                dbHelper.createPlaylist(playlistName)
                val data = ContentValues()
                data.put("name", playlistName)
                db.insert("playlists", null, data)
                inputEditText.setText("")
                inputEditText.hint = "Name"
                addBtn.visibility = VISIBLE
            } else{
                Toast.makeText(appContext, R.string.isEmpty, Toast.LENGTH_SHORT).show()
                addBtn.visibility = VISIBLE
            }
            initRecyclerView()
        }

    }



    private fun initRecyclerView() {
        db = dbHelper.readableDatabase
        val dbAdapter = RecyclerPlaylistByDB()
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM playlists",null
        )
        dbAdapter.rvSet(appContext, cursor)
        val manager = LinearLayoutManager(requireContext())
        binding.rvPlaylist.apply {
            layoutManager = manager
            adapter = dbAdapter
        }
    }

    private fun initAdsView() {
        val appContext= requireContext().applicationContext
        MobileAds.initialize(appContext) {}
        val adView = binding.adView2
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }



}