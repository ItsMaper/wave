package com.coffenow.wave.adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.R
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.ItemPlaylistBinding
import com.coffenow.wave.databinding.OnlineMusicBinding
import com.coffenow.wave.databinding.PlayerItemPlaylistBinding
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.DBPlaylistModel
import com.coffenow.wave.model.YTModel

class RecyclerSearchesByDB : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    lateinit var context: Context
    private lateinit var cursor: Cursor
    private var items = ArrayList<DBModel>()

    fun rvSet(context: Context, cursor: Cursor){
        this.context=context
        this.cursor=cursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = OnlineMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        cursor.moveToPosition(position)
        val videoID = cursor.getString(0)
        val name = cursor.getString(1)
        val publisher = cursor.getString(2)
        val thumb = cursor.getString(3)
        items.add(DBModel(videoID,name,publisher,thumb))
        (holder as ItemHolder).setData(items[position])
    }

    inner class ItemHolder (itemView: OnlineMusicBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView
        fun setData(data:DBModel){
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("type", "web")
                i.putExtra("id", data.id)
                i.putExtra("title", data.name)
                i.putExtra("publisher", data.channelName)
                i.putExtra("thumbnail", data.thumb)
                it.context.startActivity(i)
            }
            binding.onlineTitle.text =data.name
            binding.onlinePublisher.text =data.channelName
            Glide.with(context)
                .load(data.thumb)
                .into(binding.onlineThumbnail)
        }

    }
    override fun getItemCount(): Int {
        return cursor.count
    }
}

class RecyclerPlayerPlaylistByDB : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    lateinit var context: Context
    private lateinit var cursor: Cursor
    var currentSelected: Int? = 0
    var addListener: ItemClickListener? = null
    private var playerItems = ArrayList<DBModel>()

    fun rvSet(context: Context, cursor: Cursor){
        this.context=context
        this.cursor=cursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = PlayerItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(view)
    }
    override fun getItemCount(): Int {
        return cursor.count
    }
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        cursor.moveToPosition(position)
        val videoID = cursor.getString(0)
        val name = cursor.getString(1)
        val publisher = cursor.getString(2)
        val thumb = cursor.getString(3)
        playerItems.add(DBModel(videoID,name,publisher,thumb))
        val function = { pos: Int ->
            if (currentSelected == null || currentSelected != pos) {
                currentSelected = pos
                notifyDataSetChanged()
            }
        }
        (holder as ItemHolder).setData(playerItems[position],position == currentSelected, function, position)
    }

    inner class ItemHolder (itemView: PlayerItemPlaylistBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView
        fun setData(data:DBModel, selected: Boolean, function: (Int) -> Unit, position: Int){
            binding.root.isSelected = selected
            binding.root.setOnClickListener {
                function(position)
                if (!selected){
                    addListener?.onClick(data)
                }
            }
            binding.tvPpTitle.text =data.name
            Glide.with(context)
                .load(data.thumb)
                .into(binding.ppThumbnail)
        }

    }

    fun interface ItemClickListener {
        fun onClick(data: DBModel)
    }
}


class RecyclerPlaylistByDB : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    lateinit var context: Context
    private lateinit var cursor: Cursor
    private var items = ArrayList<DBPlaylistModel>()

    fun rvSet(context: Context, cursor: Cursor){
        this.context=context
        this.cursor=cursor
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ItemHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        cursor.moveToPosition(position)
        val name = cursor.getString(0)
        items.add(DBPlaylistModel(name))
        (holder as ItemHolder).setData(items[position])
    }

    inner class ItemHolder (itemView: ItemPlaylistBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView
        fun setData(data:DBPlaylistModel){
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("type", "web")
                i.putExtra("playlist" , data.name)
                it.context.startActivity(i)
            }
            binding.playlistTitle.text= data.name.replaceFirstChar{data.name[0].titlecase()}
            binding.playlistCount.text = cursor.count.toString()
            binding.playlistThumbnail.setImageResource(R.drawable.ic_baseline_wave_list)
        }
    }
    override fun getItemCount(): Int {
        return cursor.count
    }
}
