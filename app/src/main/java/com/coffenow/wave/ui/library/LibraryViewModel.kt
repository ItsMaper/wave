package com.coffenow.wave.ui.library

import android.database.Cursor
import androidx.lifecycle.ViewModel
import com.coffenow.wave.model.DBPlaylistModel

class LibraryViewModel : ViewModel() {
    private val _data = ArrayList<DBPlaylistModel>()
    val dataLoaded = _data

    fun parseDBData(cursor: Cursor){
        var i = 0
        if (cursor.count == 1){
            cursor.moveToFirst()
            val title = cursor.getString(0)
            dataLoaded.add(DBPlaylistModel(title))
        }else{
            if (cursor.count != 0){
                while (i < cursor.count-1){
                    cursor.moveToPosition(i)
                    val title = cursor.getString(0)
                    dataLoaded.add(DBPlaylistModel(title))
                    i++
                }
            }
        }

    }

    fun clearAll(){
        _data.clear()
    }
}