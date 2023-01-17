package com.coffenow.wave.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.OnlineMusicBinding
import com.coffenow.wave.diffutils.VideoDiffUtil
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.YTModel

class OnlineMusicAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<DBModel.Items>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = OnlineMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OnlineHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OnlineHolder).setData(items[position])
    }
    override fun getItemCount(): Int = items.size

    class OnlineHolder(itemView: OnlineMusicBinding) : RecyclerView.ViewHolder(itemView.root){
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
                it.context.startActivity(i)
            }
            binding.downloadMusic.setOnClickListener{
                val url = "https://convert2mp3s.com/api/single/mp3?url=$videoID"
            }
            binding.onlineTitle.text = data.title
            binding.onlinePublisher.text = data.channelName
            Glide.with(binding.root)
                .load(data.thumb)
                .into(binding.onlineThumbnail)
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



