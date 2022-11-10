package com.coffenow.wave.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.databinding.ItemVideoBinding
import com.coffenow.wave.diffutils.VideoDiffUtil
import com.coffenow.wave.model.YTModel


class VideoAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var oldItems = emptyList<YTModel.Items>()

    class VideoHolder(itemView: ItemVideoBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView

        fun setData(data: YTModel.Items){
            binding.tvVideoTitle.text = data.snippet.title
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

    fun setData(newList: List<YTModel.Items>){
        val videoDiff = VideoDiffUtil(oldItems, newList)
        val diff = DiffUtil.calculateDiff(videoDiff)
        oldItems = newList
        diff.dispatchUpdatesTo(this)
    }

}