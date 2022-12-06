package com.coffenow.wave.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import com.bumptech.glide.Glide
import com.coffenow.wave.databinding.ItemVideoBinding
import com.coffenow.wave.diffutils.PlaylistItemDiffUtil
import com.coffenow.wave.model.YTModelPlayLists

class PlaylistItemAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>(){

    private val oldItems = ArrayList<YTModelPlayLists.PlaylistItem>()
    var currentSelected: Int? = 0
    var addListener: ItemClickListener? = null

    inner class PlaylistItemHolder(itemView: ItemVideoBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        private val binding = itemView
        fun setData(data: YTModelPlayLists.PlaylistItem, selected: Boolean,
                    function: (Int) -> Unit, position: Int) {

            binding.root.isSelected = selected
            binding.root.setOnClickListener {
                function(position)
                if (!selected){
                    addListener?.onClick(data)
                }
            }

            binding.tvVideoTitle.text = data.snippetYt.title
            binding.tvPublisher.text = data.snippetYt.publishedAt
            Glide.with(binding.root)
                .load(data.snippetYt.thumbnails.high.url)
                .into(binding.tvThumbnail)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return PlaylistItemHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val function = { pos: Int ->
            if (currentSelected == null || currentSelected != pos) {
                currentSelected = pos
                notifyDataSetChanged()
            }
        }
        (holder as PlaylistItemHolder).setData(oldItems[position],position == currentSelected, function, position)
    }

    override fun getItemCount(): Int = oldItems.size

    fun setData(newList: List<YTModelPlayLists.PlaylistItem>, rv: RecyclerView){
        val videoDiff = PlaylistItemDiffUtil(oldItems, newList)
        val diff = DiffUtil.calculateDiff(videoDiff)
        oldItems.addAll(newList)
        diff.dispatchUpdatesTo(this)
        rv.scrollToPosition(oldItems.size - newList.size)
    }

    fun interface ItemClickListener {
        fun onClick(data: YTModelPlayLists.PlaylistItem)
    }



}