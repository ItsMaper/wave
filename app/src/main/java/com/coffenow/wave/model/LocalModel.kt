package com.coffenow.wave.model

import android.net.Uri

data class LocalModel(
    val id: Long,
    val title:String,
    val uri: Uri )

val AudioList = mutableListOf<LocalModel>()
