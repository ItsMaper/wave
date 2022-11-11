package com.coffenow.wave.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.adapter.VideoAdapter
import com.coffenow.wave.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var videoViewModel: HomeViewModel? = null
    private val adapter = VideoAdapter()
    private var isLoading = false
    private var isScroll = false
    private var currentItem = -1
    private var totalItem = -1
    private var scrollOutItem = -1
    private var isAllVideoLoaded = false

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val manager = LinearLayoutManager(requireContext())
        binding.rvVideo.adapter = adapter
        binding.rvVideo.layoutManager = manager

        binding.rvVideo.addOnScrollListener(object : RecyclerView.OnScrollListener(){
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
                            videoViewModel?.getVideoList()
                        } else {
                            Toast.makeText(requireContext(), "All video loaded", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

        })

        videoViewModel?.video?.observe(viewLifecycleOwner) {
            if (it != null && it.items.isNotEmpty()) {
                adapter.setData(it.items, binding.rvVideo)
            }
        }

        videoViewModel?.isLoading?.observe(viewLifecycleOwner) {
            isLoading = it
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        videoViewModel?.isAllVideoLoaded?.observe(viewLifecycleOwner) {
            isAllVideoLoaded = it
            if (it) Toast.makeText(
                requireContext(),
                "All video has been loaded",
                Toast.LENGTH_SHORT
            ).show()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        videoViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
}