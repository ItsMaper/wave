package com.coffenow.wave.ymodel

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class SnippetYT (
    @SerializedName("title")
    val title: String,

    @SerializedName("description")
    val description:String,

    @SerializedName("customUrl")
    val customUrl: String,

    @SerializedName("publishedAt")
    val publishedAt:String,

    @SerializedName("thumbnails")
    val thumbnails: ThumbnailsYT,

    @SerializedName("country")
    val country:String
        )