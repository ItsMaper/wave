package com.coffenow.wave.model

import com.google.gson.annotations.SerializedName

data class YTModelPlaylistItem(
    @SerializedName("nextPageToken")
    val nextPageToken: String?,

    @SerializedName("items")
    val items: List<YTModelPlayLists.PlaylistItem>
)