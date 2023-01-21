package com.coffenow.wave.adapter

import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.view.LayoutInflater
import android.view.View.GONE
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.R
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.ItemPlaylistBinding
import com.coffenow.wave.utils.WaveDBHelper
import com.coffenow.wave.model.DBPlaylistModel

class PlaylistsByDB : RecyclerView.Adapter<RecyclerView.ViewHolder>(){
    lateinit var context: Context
    private var items = ArrayList<DBPlaylistModel>()
    private var toAdd : Boolean = false

    fun rvSet(context: Context, toAdd: Boolean, data: ArrayList<DBPlaylistModel>){
        this.context=context
        this.items=data
        this.toAdd = toAdd
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as DataHolder).setData(items[position])
    }
    override fun getItemCount(): Int {
        return items.count()
    }

    inner class DataHolder (itemView: ItemPlaylistBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView
        fun setData(data:DBPlaylistModel){
            binding.root.setOnClickListener {
                if(getPlaylistCount(data.title) > 0){
                    val i = Intent(it.context, PlayerActivity::class.java)
                    i.putExtra("playlist" , data.title)
                    it.context.startActivity(i)
                } else{
                    Toast.makeText(context, "You need add items to load", Toast.LENGTH_SHORT).show()
                }
            }
            binding.playlistTitle.text= data.title.replaceFirstChar{data.title[0].titlecase()}
            binding.playlistCount.text = "%s Waves".format(getPlaylistCount(data.title))
            binding.playlistThumbnail.setImageResource(R.drawable.ic_baseline_wave_list)
            if (!toAdd){
                binding.addToBTN.visibility = GONE
            }
        }

        private fun getPlaylistCount(title: String) : Int {
            val db:SQLiteDatabase = WaveDBHelper(context).readableDatabase
            val cursor: Cursor = db.rawQuery(
                "SELECT * FROM $title",null
            )
            return cursor.count
        }
    }

    fun updateRecycler(items : List<DBPlaylistModel>){
        this.items = items as ArrayList<DBPlaylistModel>
        notifyDataSetChanged()
    }
}
