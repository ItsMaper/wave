package com.coffenow.wave.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.View.INVISIBLE
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.ItemMusicBinding
import com.coffenow.wave.diffutils.VideoDiffUtil
import com.coffenow.wave.model.DBModel

class ItemMusicAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<DBModel.Items>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DataHolder).setData(items[position])
    }
    override fun getItemCount(): Int = items.size

    class DataHolder(itemView: ItemMusicBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView
        fun setData(data: DBModel.Items){
            val videoID= data.id
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("playlist","default")
                i.putExtra("id", videoID)
                i.putExtra("title", data.title)
                i.putExtra("publisher", data.channelName)
                i.putExtra("thumbnail", data.thumb)
                i.putExtra("state", data.live)
                it.context.startActivity(i)
            }
            binding.downloadMusic.setOnClickListener{
                val url = "https://convert2mp3s.com/api/single/mp3?url=$videoID"
            }
            binding.onlineTitle.text = data.title
            binding.onlinePublisher.text = data.channelName
            Glide.with(binding.root)
                .load(data.thumb)
                .centerCrop()
                .into(binding.onlineThumbnail)
            if (data.live != "live"){
                binding.stateIV.visibility = INVISIBLE
            }
        }
    }

    fun setDataDiff(newList: List<DBModel.Items>, rv: RecyclerView){
        val videoDiff = VideoDiffUtil(items, newList)
        val diff = DiffUtil.calculateDiff(videoDiff)
        items.addAll(newList)
        diff.dispatchUpdatesTo(this)
        rv.scrollToPosition(items.size - newList.size)
    }

    fun clearAll(){
        items.clear()
        notifyDataSetChanged()
    }
}



