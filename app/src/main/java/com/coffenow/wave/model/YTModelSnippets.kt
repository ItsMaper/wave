package com.coffenow.wave.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YTModelSnippets(
    @SerializedName("title")
    val title: String,

    @SerializedName("thumbnails")
    val thumbnails: YTModelThumbnails,

    @SerializedName("channelTitle")
    val channelTitle: String

)