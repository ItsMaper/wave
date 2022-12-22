package com.coffenow.wave.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.PlayerItemPlaylistBinding
import com.coffenow.wave.diffutils.PlaylistDiffUtil
import com.coffenow.wave.model.YTModel

class PlayerPlaylistAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val playerItems = ArrayList<YTModel.Items>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = PlayerItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PPlaylistHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as PPlaylistHolder).setData(playerItems[position])
    }

    override fun getItemCount(): Int {
        return playerItems.size
    }
    class PPlaylistHolder(itemView: PlayerItemPlaylistBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView

        fun setData(data: YTModel.Items){
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("type", "web")
                i.putExtra("thumbnail", data.snippet.thumbnails.high.url)
                i.putExtra("title", data.snippet.title)
                i.putExtra("publisher", data.snippet.channelTitle)
                i.putExtra("id", data.videoId.videoID)
                it.context.startActivity(i)
            }
            binding.tvPpTitle.text = data.snippet.title
            Glide.with(binding.root).load(data.snippet.thumbnails.high.url)
                .into(binding.ppThumbnail)
        }
    }
    fun setDataDiff(newList: List<YTModel.Items>, rv: RecyclerView){
        val playlistDiff = PlaylistDiffUtil(playerItems, newList)
        val diff = DiffUtil.calculateDiff(playlistDiff)
        playerItems.addAll(newList)
        diff.dispatchUpdatesTo(this)
        rv.scrollToPosition(playerItems.size - newList.size)
    }
    fun clearAll(){
        playerItems.clear()
    }
}