package com.coffenow.wave.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YTModelPlayLists(
    @SerializedName("nextPageToken")
    val nextPageToken: String?,
    @SerializedName("items")
    val items: List<PlaylistItem>
) {
    data class PlaylistItem(
        @SerializedName("id")
        val id: String,
        @SerializedName("snippet")
        val snippetYt: YTModelSnippets,
        @SerializedName("contentDetails")
        val contentDetail: ContentDetail
    )
    data class ContentDetail(
        @SerializedName("itemCount")
        val itemCount: Int
    )
}