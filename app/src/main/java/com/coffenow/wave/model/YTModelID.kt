package com.coffenow.wave.model

import androidx.annotation.Keep
import com.google.gson.annotations.SerializedName

@Keep
data class YTModelID (
    @SerializedName("videoId")
    val videoID: String?
        )
