package com.coffenow.wave.ui.home

import android.app.Activity
import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.AbsListView
import androidx.core.content.ContextCompat.getSystemService
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.HORIZONTAL
import com.coffenow.wave.R
import com.coffenow.wave.adapter.CircularAdapter
import com.coffenow.wave.adapter.ItemMusicAdapter
import com.coffenow.wave.databinding.FragmentHomeBinding
import com.coffenow.wave.utils.WaveDBHelper


class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var viewModel: HomeViewModel? = null
    private val musicAdapter = ItemMusicAdapter()
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db:SQLiteDatabase
    private lateinit var appContext : Context
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appContext= requireContext().applicationContext
        dbHelper = WaveDBHelper(appContext)
        getViewModel()
        circleRecyclerView()
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

    private fun circleRecyclerView(){
        val items = ArrayList<String>()
        items.add("Pop")
        items.add("Rock")
        items.add("Lo-Fi")
        items.add("Ambient")
        items.add("Jazz")
        items.add("Blues")
        items.add("Gospel")
        items.add("House")
        items.add("Rap")
        items.add("Dance")
        items.add("Country")
        items.add("Hard Rock")
        items.add("Steampunk")
        items.add("Orchestral")

        val circularAdapter = CircularAdapter(items)
        val manager = LinearLayoutManager(appContext, HORIZONTAL, false)
        binding.circularRV.apply {
            adapter = circularAdapter
            layoutManager = manager
        }
    }

    private fun initRecyclerView() {
        val manager = LinearLayoutManager(requireContext())
        binding.musicRV.apply {
            adapter = musicAdapter
            layoutManager = manager
            addOnScrollListener(object : RecyclerView.OnScrollListener(){
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == AbsListView.OnScrollListener.SCROLL_STATE_TOUCH_SCROLL){
                        isScroll = true }
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
                    musicAdapter.clearAll()
                    musicAdapter.setDataDiff(it.items, binding.musicRV) } } }
    }

        private fun setSearch() {
            val searchView = binding.svMusic
            searchView.setOnEditorActionListener { v, actionId, _->
                var handled = false
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    if (v.text.toString().isNotEmpty()){
                        viewModel?.nextPageToken = null
                        viewModel?.querySearch = v.text.toString()
                        musicAdapter.clearAll()
                        viewModel?.getApiDataQuery()
                        binding.waveTV.visibility = VISIBLE
                        binding.musicRV.scrollToPosition(0) }
                    if (v.text.toString().isEmpty()){
                        viewModel?.querySearch = resources.getString(R.string.search_bar)
                        viewModel?.nextPageToken = null
                        musicAdapter.clearAll()
                        getViewModel()
                        binding.waveTV.visibility = VISIBLE
                        binding.musicRV.scrollToPosition(0) }
                    v.text =""
                    searchView.clearFocus()
                    v.hideKeyboard()
                    handled = true
                }
                handled
            }
        }
    fun View.hideKeyboard() {
        val inputManager = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(windowToken, 0)
    }
    override fun onResume() {
        super.onResume()
        binding.musicRV.scrollToPosition(0)
    }
    override fun onDestroy() {
        super.onDestroy()
        dbHelper.close()
    }
}



