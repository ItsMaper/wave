package com.coffenow.wave.diffutils

import androidx.recyclerview.widget.DiffUtil
import com.coffenow.wave.model.YTModel


class PlaylistDiffUtil(
    private val oldList: ArrayList<YTModel.Items>,
    private val newList: List<YTModel.Items>
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
        return oldVideo.snippet.title == newVideo.snippet.title
    }
}