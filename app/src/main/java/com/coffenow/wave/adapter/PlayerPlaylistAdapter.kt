package com.coffenow.wave.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import androidx.lifecycle.MutableLiveData
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.databinding.PlayerItemPlaylistBinding
import com.coffenow.wave.diffutils.PlaylistDiffUtil
import com.coffenow.wave.model.DBModel

class PlayerPlaylistAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val playerItems = ArrayList<DBModel.Items>()
    var currentSelected :  MutableLiveData<Int> = MutableLiveData(0)
    var addListener: ItemClickListener? = null

    companion object{
        val itemsSize = MutableLiveData<Int>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = PlayerItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DataHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val function = { pos: Int ->
            if (currentSelected.value == null || currentSelected.value != pos) {
                currentSelected = MutableLiveData(pos)
                notifyDataSetChanged()}
        }
        (holder as DataHolder).setData(playerItems[position],position == currentSelected.value, function, position)
    }
    override fun getItemCount(): Int {
        itemsSize.value = playerItems.size
        return playerItems.size
    }

    inner class DataHolder(itemView: PlayerItemPlaylistBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView

        fun setData(data: DBModel.Items, selected: Boolean,
                    function: (Int) -> Unit, position: Int) {

            binding.root.isSelected = selected
            binding.root.setOnClickListener {
                function(position)
                if (!selected){
                    addListener?.onClick(data)
                }
            }
            binding.tvPpTitle.text = data.title
            Glide.with(binding.root)
                .load(data.thumb)
                .centerCrop()
                .into(binding.ppThumbnail)
            if (data.live != "live"){
                binding.stateIV.visibility = View.INVISIBLE
            }
        }
    }
    fun setDataDiff(newList: List<DBModel.Items>, rv: RecyclerView){
        val playlistDiff = PlaylistDiffUtil(playerItems, newList)
        val diff = DiffUtil.calculateDiff(playlistDiff)
        playerItems.addAll(newList)
        diff.dispatchUpdatesTo(this)
        rv.scrollToPosition(playerItems.size - newList.size)
    }

    fun clearAll(){
        playerItems.clear()
    }
    fun interface ItemClickListener {
        fun onClick(data: DBModel.Items)
    }
}