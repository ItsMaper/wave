package com.coffenow.wave.model

data class LocalModel(
    val id:String,
    val title:String,
    val duration: Long = 0,
    val path: String,
    val artUri:String)