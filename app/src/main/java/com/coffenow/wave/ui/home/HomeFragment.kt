package com.coffenow.wave.ui.home

import android.app.SearchManager
import android.content.ContentUris
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.widget.AbsListView
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.appcompat.widget.SearchView
import androidx.core.content.ContentProviderCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.R
import com.coffenow.wave.adapter.LocalMusicAdapter
import com.coffenow.wave.adapter.OnlineMusicAdapter
import com.coffenow.wave.databinding.FragmentHomeBinding
import com.coffenow.wave.model.LocalModel
import java.io.File

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var viewModel: HomeViewModel? = null
    private val onlineAdapter = OnlineMusicAdapter()
    private val localAdapter = LocalMusicAdapter()
    private var isLoading = false
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1
    private var isAllVideoLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val appContext= requireContext().applicationContext

        binding.rvLocalMusic.visibility = INVISIBLE
        binding.rvOnlineMusic.visibility = INVISIBLE
        setSearch()
        initOnlineRecyclerView()
        initLocalRecyclerView()
        if (netState(appContext)){
            binding.rvOnlineMusic.visibility= VISIBLE
        }else{
            binding.rvLocalMusic.visibility= VISIBLE
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root }



    private fun netState(context: Context): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val capabilities = connectivityManager.getNetworkCapabilities(connectivityManager.activeNetwork)
        if (capabilities != null) {
            if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return true
            } else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) {
                return true
            }
        }
        return false
    }

    private fun initLocalRecyclerView() {
        val manager = LinearLayoutManager(requireContext())
        binding.rvLocalMusic.apply {
            adapter =localAdapter
            layoutManager =manager
            addOnScrollListener(object :RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                        isScroll = true
                        rvScrollState() }
                    else{ rvStaticState()} }
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    currentItem = manager.childCount
                    totalItem = manager.itemCount
                    scrollOutItem = manager.findFirstVisibleItemPosition() } }) }

        binding.localPlayer.setOnClickListener {
            binding.rvOnlineMusic.visibility= INVISIBLE
            binding.rvLocalMusic.visibility = VISIBLE }
    }

    private fun initOnlineRecyclerView() {
        val manager = LinearLayoutManager(requireContext())
        binding.rvOnlineMusic.apply {
            adapter = onlineAdapter
            layoutManager = manager
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                        isScroll = true
                        rvScrollState() }
                    else{ rvStaticState()} }
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    currentItem = manager.childCount
                    totalItem = manager.itemCount
                    scrollOutItem = manager.findFirstVisibleItemPosition()
                    if (isScroll && (currentItem + scrollOutItem == totalItem)){
                        isScroll = false
                        if (!isLoading && totalItem<=23){
                            if (!isAllVideoLoaded){ viewModel?.getOnlineList() } } } } })
            viewModel?.online_data?.observe(viewLifecycleOwner) {
                if (it != null && it.items.isNotEmpty()) {
                    onlineAdapter.setDataDiff(it.items, binding.rvOnlineMusic) } } }

        binding.onlinePlayer.setOnClickListener {
            binding.rvLocalMusic.visibility= INVISIBLE
            binding.rvOnlineMusic.visibility = VISIBLE }
    }


    private fun setSearch() {
        val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
        val searchView = binding.searchView
        searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
        searchView.queryHint = resources.getString(R.string.search_bar)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(q: String): Boolean {
                binding.sectionButtons.visibility = INVISIBLE
                if (q.isNotEmpty()){
                    viewModel?.querySearch = q
                    viewModel?.nextPageToken = null
                    onlineAdapter.clearAll()
                    viewModel?.getOnlineList() }
                return true }
            override fun onQueryTextChange(newText: String): Boolean {
                binding.sectionButtons.visibility = INVISIBLE
                if (newText.isEmpty()){
                    viewModel?.querySearch = null
                    viewModel?.nextPageToken = null
                    onlineAdapter.clearAll()
                    viewModel?.getOnlineList() }
                return false } })
    }

    private fun rvStaticState() {
        binding.searchView.translationY=5F
        binding.searchView.visibility= VISIBLE
        binding.sectionButtons.visibility = VISIBLE
        if (scrollOutItem <= 0){
            binding.rvOnlineMusic.translationY=25F }
    }
    private fun rvScrollState() {
        binding.rvOnlineMusic.translationY=-100F
        binding.searchView.visibility= INVISIBLE
        binding.sectionButtons.visibility = INVISIBLE
    }
}



