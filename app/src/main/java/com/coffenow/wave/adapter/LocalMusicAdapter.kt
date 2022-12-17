package com.coffenow.wave.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.LocalMusicBinding
import com.coffenow.wave.model.LocalModel

class LocalMusicAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var localItems = ArrayList<LocalModel>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LocalMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LocalMusicAdapter.LocalHolder).setData(localItems[position])
    }

    class LocalHolder(itemView: LocalMusicBinding) : RecyclerView.ViewHolder(itemView.root) {
        private val binding = itemView

        fun setData(data: LocalModel){
            val videoID= data.id
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("type", "local")
                i.putExtra("video_title", data.title)
                it.context.startActivity(i)
            }
            binding.localTitle.text = data.title
        }
    }

    override fun getItemCount(): Int = localItems.size


}