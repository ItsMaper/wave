package com.coffenow.wave.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.databinding.ItemPlaylistBinding
import com.coffenow.wave.diffutils.PlaylistDiffUtil
import com.coffenow.wave.model.YTModel

class PlaylistAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val oldItems = ArrayList<YTModel.Items>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PlaylistHolder).setData(oldItems[position])
    }

    override fun getItemCount(): Int {
        return oldItems.size
    }
    class PlaylistHolder(itemView: ItemPlaylistBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView

        fun setData(data: YTModel.Items){
            binding.tvPlaylistTitle.text = data.snippet.title
            binding.tvVideoCount.text = "20 Videos"
            Glide.with(binding.root).load(data.snippet.thumbnails.high.url)
                .into(binding.thumbnail)
        }
    }
    fun setDataDiff(newList: List<YTModel.Items>, rv: RecyclerView){
        val playlistDiff = PlaylistDiffUtil(oldItems, newList)
        val diff = DiffUtil.calculateDiff(playlistDiff)
        oldItems.addAll(newList)
        diff.dispatchUpdatesTo(this)
        rv.scrollToPosition(oldItems.size - newList.size)
    }
    fun clearAll(){
        oldItems.clear()
    }
}