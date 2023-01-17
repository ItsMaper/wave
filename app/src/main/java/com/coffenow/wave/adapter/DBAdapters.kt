package com.coffenow.wave.adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.R
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.ItemPlaylistBinding
import com.coffenow.wave.databinding.OnlineMusicBinding
import com.coffenow.wave.databinding.PlayerItemPlaylistBinding
import com.coffenow.wave.db.WaveDBHelper
import com.coffenow.wave.diffutils.PlaylistDiffUtil
import com.coffenow.wave.model.DBModel
import com.coffenow.wave.model.DBPlaylistModel

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
                if(getCount(data.title) > 0){
                    val i = Intent(it.context, PlayerActivity::class.java)
                    i.putExtra("playlist" , data.title)
                    it.context.startActivity(i)
                } else{
                    Toast.makeText(context, "You need add items to load", Toast.LENGTH_SHORT).show()
                }
            }

            binding.playlistTitle.text= data.title.replaceFirstChar{data.title[0].titlecase()}
            binding.playlistCount.text = "%s Waves".format(getCount(data.title))
            binding.playlistThumbnail.setImageResource(R.drawable.ic_baseline_wave_list)
        }

        private fun getCount(title: String) : Int {
            val db:SQLiteDatabase = WaveDBHelper(context).readableDatabase
            val cursor: Cursor = db.rawQuery(
                "SELECT * FROM $title",null
            )
            return cursor.count
        }
    }
    override fun getItemCount(): Int {
        return cursor.count
    }
}
