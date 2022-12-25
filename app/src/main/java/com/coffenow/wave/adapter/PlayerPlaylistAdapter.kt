package com.coffenow.wave.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.coffenow.wave.databinding.PlayerItemPlaylistBinding
import com.coffenow.wave.diffutils.PlaylistDiffUtil
import com.coffenow.wave.model.YTModel

class PlayerPlaylistAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val playerItems = ArrayList<YTModel.Items>()
    var currentSelected: Int? = 0
    var addListener: ItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = PlayerItemPlaylistBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PPlaylistHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val function = { pos: Int ->
            if (currentSelected == null || currentSelected != pos) {
                currentSelected = pos
                notifyDataSetChanged()
            }
        }
        (holder as PPlaylistHolder).setData(playerItems[position],position == currentSelected, function, position)
    }
    override fun getItemCount(): Int = playerItems.size

    inner class PPlaylistHolder(itemView: PlayerItemPlaylistBinding) : RecyclerView.ViewHolder(itemView.root){
        private val binding = itemView

        fun setData(data: YTModel.Items, selected: Boolean,
                    function: (Int) -> Unit, position: Int) {

            binding.root.isSelected = selected
            binding.root.setOnClickListener {
                function(position)
                if (!selected){
                    addListener?.onClick(data)
                }
            }
            binding.tvPpTitle.text = data.snippet.title
            Glide.with(binding.root)
                .load(data.snippet.thumbnails.high.url)
                .into(binding.ppThumbnail)
        }
    }
    fun setDataDiff(newList: List<YTModel.Items>, rv: RecyclerView){
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
        fun onClick(data: YTModel.Items)
    }
}