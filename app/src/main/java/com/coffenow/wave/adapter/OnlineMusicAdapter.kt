package com.coffenow.wave.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.OnlineMusicBinding
import com.coffenow.wave.diffutils.VideoDiffUtil
import com.coffenow.wave.model.YTModel

class OnlineMusicAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val items = ArrayList<YTModel.Items>()

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
        fun setData(data: YTModel.Items){
            val videoID= data.videoId.videoID
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("type", "web")
                i.putExtra("playlist","default")
                i.putExtra("id", videoID)
                i.putExtra("title", data.snippet.title)
                i.putExtra("publisher", data.snippet.channelTitle)
                i.putExtra("thumbnail", data.snippet.thumbnails.high.url)
                it.context.startActivity(i)
            }
            binding.downloadMusic.setOnClickListener{
                val url = "https://convert2mp3s.com/api/single/mp3?url=$videoID"
            }
            binding.onlineTitle.text = data.snippet.title
            binding.onlinePublisher.text = data.snippet.channelTitle
            Glide.with(binding.root)
                .load(data.snippet.thumbnails.high.url)
                .into(binding.onlineThumbnail)
        }
    }

    fun setDataDiff(newList: List<YTModel.Items>, rv: RecyclerView){
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



