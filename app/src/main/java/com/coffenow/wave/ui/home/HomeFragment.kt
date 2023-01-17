package com.coffenow.wave.ui.home

import android.app.SearchManager
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.*
import android.widget.AbsListView
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.R
import com.coffenow.wave.adapter.OnlineMusicAdapter
import com.coffenow.wave.databinding.FragmentHomeBinding
import com.coffenow.wave.db.WaveDBHelper

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var viewModel: HomeViewModel? = null
    private val onlineAdapter = OnlineMusicAdapter()
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db:SQLiteDatabase
    private lateinit var appContext : Context
    private var isLoading = false
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1
    private var isAllVideoLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appContext= requireContext().applicationContext
        dbHelper = WaveDBHelper(appContext)
        getViewModel()
        initRecyclerView()
        setSearch()
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
                return true } }
        return false }


    private fun getViewModel(){
        viewModel?.clearAll()
        db = dbHelper.readableDatabase
        val cursor: Cursor = db.rawQuery("SELECT * FROM searches", null)
        if (cursor.moveToFirst()){
            val rand: Int = (0 until (cursor.count-1)).random()
            cursor.move(rand)
            viewModel?.relatedTo = cursor.getString(0)
            viewModel?.getApiDataRelated()
            viewModel?.parseDBData(cursor)
        }else{
            viewModel?.querySearch = resources.getString(R.string.search_bar)
            viewModel?.getApiDataQuery()
        }
    }

    private fun initRecyclerView() {
            val manager = LinearLayoutManager(requireContext())
            binding.rvOnlineMusic.apply {
                adapter = onlineAdapter
                layoutManager = manager
                addOnScrollListener(object : RecyclerView.OnScrollListener(){
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
                              } } })

                viewModel?.dataLoaded?.observe(viewLifecycleOwner) {
                    if (it != null && it.items.isNotEmpty()) {
                        onlineAdapter.clearAll()
                        onlineAdapter.setDataDiff(it.items, binding.rvOnlineMusic)
                    } } } }

        private fun setSearch() {
            val searchManager = requireActivity().getSystemService(Context.SEARCH_SERVICE) as SearchManager
            val searchView = binding.svMusic
            searchView.setSearchableInfo(searchManager.getSearchableInfo(requireActivity().componentName))
            searchView.queryHint = resources.getString(R.string.search_bar)
            searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(q: String): Boolean {
                    if (q.isNotEmpty()){
                        viewModel?.nextPageToken = null
                        viewModel?.querySearch = q
                        onlineAdapter.clearAll()
                        searchView.clearFocus()
                        viewModel?.getApiDataQuery()
                        binding.rvOnlineMusic.scrollToPosition(0) }
                    return true }
                override fun onQueryTextChange(newText: String): Boolean {
                    if (newText.isEmpty()){
                        viewModel?.querySearch = resources.getString(R.string.search_bar)
                        viewModel?.nextPageToken = null
                        onlineAdapter.clearAll()
                        getViewModel()
                        searchView.clearFocus()
                        binding.rvOnlineMusic.scrollToPosition(0) }
                    return false } }) }

    override fun onResume() {
        super.onResume()
        binding.rvOnlineMusic.scrollToPosition(0)
    }
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}



