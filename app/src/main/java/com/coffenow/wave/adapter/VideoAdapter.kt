package com.coffenow.wave.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.ItemVideoBinding
import com.coffenow.wave.diffutils.VideoDiffUtil
import com.coffenow.wave.model.YTModel

class VideoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var oldItems =  ArrayList<YTModel.Items>()

    class VideoHolder(itemView: ItemVideoBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView

        fun setData(data: YTModel.Items){
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("video_img", data.snippet.thumbnails.high.url)
                i.putExtra("video_title", data.snippet.title)
                i.putExtra("channelTitle", data.snippet.channelTitle)
                i.putExtra("videoId", data.videoId.videoID)
                it.context.startActivity(i)
            }
            binding.tvVideoTitle.text = data.snippet.title
            binding.tvPublisher.text = data.snippet.channelTitle
            Glide.with(binding.root)
                .load(data.snippet.thumbnails.high.url)
                .into(binding.tvThumbnail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as VideoHolder).setData(oldItems[position])
    }

    override fun getItemCount(): Int {
        return oldItems.size
    }

    fun setData(newList: List<YTModel.Items>, rv: RecyclerView){
        val videoDiff = VideoDiffUtil(oldItems, newList)
        val diff = DiffUtil.calculateDiff(videoDiff)
        oldItems.addAll(newList)
        diff.dispatchUpdatesTo(this)
        rv.scrollToPosition(oldItems.size - newList.size)
    }

    fun clearAll(){
        oldItems.clear()
        notifyDataSetChanged()
    }
}

