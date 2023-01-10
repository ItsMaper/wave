package com.coffenow.wave.adapter

import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.databinding.AddToPlaylistBinding

class AddToPlaylist(private val cursor: Cursor) : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    private var items = ArrayList<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = AddToPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return Render(view)
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        cursor.moveToPosition(position)
        val name = cursor.getString(0)
        items.add(name)
        (holder as Render).setData(items[position])
    }

    override fun getItemCount(): Int  = items.size

    inner class Render(itemView: AddToPlaylistBinding) : RecyclerView.ViewHolder(itemView.root) {
        val binding = itemView
        fun setData(data : String){
            binding.playlistTitle.text = data
        }
    }

}