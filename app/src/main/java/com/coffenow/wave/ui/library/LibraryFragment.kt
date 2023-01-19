package com.coffenow.wave.ui.library

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.SearchView
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.coffenow.wave.R
import com.coffenow.wave.adapter.PlaylistsByDB
import com.coffenow.wave.databinding.FragmentLibraryBinding
import com.coffenow.wave.utils.WaveDBHelper
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds


class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private var viewModel: LibraryViewModel? = null
    private val dbAdapter = PlaylistsByDB()
    private lateinit var appContext : Context
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db: SQLiteDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBinding()
        initRecyclerView()
        setSearch()
        initAdsView()
         }

    private fun setBinding(){
        appContext= requireContext().applicationContext
        dbHelper = WaveDBHelper(appContext)
    }

    private fun initRecyclerView() {
        viewModel?.clearAll()
        db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM playlists",null)
        viewModel?.parseDBData(cursor)
        dbAdapter.rvSet(appContext, viewModel!!.dataLoaded)
        val manager = LinearLayoutManager(requireContext())
        binding.rvPlaylist.apply {
            layoutManager = manager
            adapter = dbAdapter
        }
    }

    private fun setSearch() {
        val items = viewModel?.dataLoaded
        val searchView = binding.svMusic
        searchView.addTextChangedListener {q ->
                if (q.toString().isNotEmpty()){
                    val filter = items?.filter { it.title.lowercase().contains(q.toString().lowercase()) }
                    dbAdapter.updateRecycler(filter!!)
                    binding.rvPlaylist.scrollToPosition(0)
                 }
            if (q.toString().isEmpty()){
                searchView.clearFocus()
                initRecyclerView()
            }}
    }

    private fun initAdsView() {
        val appContext= requireContext().applicationContext
        MobileAds.initialize(appContext) {}
        val adView = binding.adView2
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }



}