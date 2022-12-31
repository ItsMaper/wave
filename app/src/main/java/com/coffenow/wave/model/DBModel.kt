package com.coffenow.wave.model

data class DBModel(
    val items: List<Items>,
){
    data class Items(
        val id: String,
        val title:String,
        val channelName: String,
        val thumb: String
    )
}
