package com.coffenow.wave.adapter

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.ItemCircularBinding

class CircularAdapter(private val items: ArrayList<String>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view =ItemCircularBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataHolder(view)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        (holder as DataHolder).setData(items[position])
    }

    class DataHolder(itemView: ItemCircularBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView
        fun setData(data: String){
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("playlist","default")
                i.putExtra("query", data)
                it.context.startActivity(i)
            }
            binding.genreTV.text = data
            Glide.with(binding.root)
                .load("https://loremflickr.com/320/320/$data-music")
                .centerCrop()
                .into(binding.imageRounded)
            binding.imageRounded.brightness = 0.7f
        }
    }
}