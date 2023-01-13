package com.coffenow.wave.db

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class WaveDBHelper(context:Context): SQLiteOpenHelper(context, "wave.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase?) {
        val searchesCreate = "CREATE TABLE IF NOT EXISTS searches " +
                "(videoID TEXT PRIMARY KEY, title TEXT, publisher TEXT, thumbnail TEXT);"
        val playlistsCreate = "CREATE TABLE IF NOT EXISTS playlists " +
                "(title TEXT PRIMARY KEY);"
        val favoritesCreate = "CREATE TABLE IF NOT EXISTS favorites " +
                "(videoID TEXT PRIMARY KEY, title TEXT, publisher TEXT, thumbnail TEXT);"
        db!!.execSQL(searchesCreate)
        db.execSQL(playlistsCreate)
        db.execSQL(favoritesCreate)
        val data = ContentValues()
        data.put("title", "favorites")
        db.insert("playlists", null, data)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val orderClear =  "DROP TABLE IF EXISTS searches;"
        db!!.execSQL(orderClear)
    }

    fun createPlaylist(tableName:String){
        val db = this.writableDatabase
        val orderCreate = "CREATE TABLE IF NOT EXISTS "+ tableName +
                " (videoID TEXT PRIMARY KEY, title TEXT, publisher TEXT, thumbnail TEXT);"
        db.execSQL(orderCreate)
    }

    fun isFilled(tableName:String): Boolean {
        val db = this.readableDatabase
        val cursor = db.rawQuery("SELECT * FROM $tableName;", null)
        if(cursor!=null){
            if (cursor.count >= 25 ){
                cursor.close()
                return true
            }
        }
        return false
    }

    fun addData(tableName: String, data: ContentValues){
        val db = this.writableDatabase
        db.insert(tableName, null, data)
        db.close()
    }
}