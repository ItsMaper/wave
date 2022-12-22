package com.coffenow.wave.ui.library

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.adapter.PlaylistAdapter
import com.coffenow.wave.databinding.FragmentLibraryBinding
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.MobileAds

class LibraryFragment : Fragment() {

    private var _binding: FragmentLibraryBinding? = null
    private val binding get() = _binding!!
    private var playlistViewModel: LibraryViewModel? = null
    private val adapter = PlaylistAdapter()
    private var isLoading = false
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1
    private var isAllVideoLoaded = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        playlistViewModel = ViewModelProvider(this)[LibraryViewModel::class.java]
        _binding = FragmentLibraryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAdsView()
        initRecyclerView()
         }

    private fun initRecyclerView() {
        val manager = LinearLayoutManager(requireContext())
        binding.rvPlaylist.adapter = adapter
        binding.rvPlaylist.layoutManager = manager

        binding.rvPlaylist.addOnScrollListener(object : RecyclerView.OnScrollListener(){
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                    isScroll = true
                }
            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)
                currentItem = manager.childCount
                totalItem = manager.itemCount
                scrollOutItem = manager.findFirstVisibleItemPosition()
                if (isScroll && (currentItem + scrollOutItem == totalItem)){
                    isScroll = false
                    if (!isLoading){
                        if (!isAllVideoLoaded){
                            playlistViewModel?.getPlaylist()
                        } else {
                            Toast.makeText(requireContext(), "All playlist loaded", Toast.LENGTH_SHORT).show()
                        } } } } })

        playlistViewModel?.playlist?.observe(viewLifecycleOwner) {
            adapter.setDataDiff(it?.items!!, binding.rvPlaylist)
            it.nextPageToken?.let { token ->
                Log.e("next page token", token) } }

        playlistViewModel?.isAllPlaylistLoaded?.observe(viewLifecycleOwner) {
            isAllVideoLoaded = it }
    }

    private fun initAdsView() {
        val appContext= requireContext().applicationContext
        MobileAds.initialize(appContext) {}
        val adView = binding.adView2
        val adRequest = AdRequest.Builder().build()
        adView.loadAd(adRequest)
    }



}