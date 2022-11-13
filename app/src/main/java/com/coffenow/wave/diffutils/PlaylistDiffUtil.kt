package com.coffenow.wave.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.coffenow.wave.model.YTModelPlayLists

class PlaylistDiffUtil(
    private val oldList: List<YTModelPlayLists.PlaylistItem>,
    private val newList: List<YTModelPlayLists.PlaylistItem>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int {
        return oldList.size
    }

    override fun getNewListSize(): Int {
        return newList.size
    }

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        val oldVideo = oldList[oldItemPosition]
        val newVideo = newList[newItemPosition]
        return oldVideo.snippetYt.title == newVideo.snippetYt.title
    }
}