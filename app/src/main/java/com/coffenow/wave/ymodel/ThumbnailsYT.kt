package com.coffenow.wave.ymodel

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName


@Keep
data class ThumbnailsYT(
    @SerializedName("high")
    val high: High
){data class High(
    @SerializedName("url")
    val url: String
)}