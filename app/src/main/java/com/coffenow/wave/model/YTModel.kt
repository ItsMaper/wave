package com.coffenow.wave.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YTModel(
    @SerializedName("nextPageToken")
    val nextPageToken: String?,
    @SerializedName("items")
    val items: List<Items>
) {
    data class Items(
        @SerializedName("id")
        val videoId: YTModelID,
        @SerializedName("snippet")
        val snippet: YTModelSnippets,
        @SerializedName("q")
        val query: String?
    )
}