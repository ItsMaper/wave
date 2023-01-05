package com.coffenow.wave.interfaces

import androidx.lifecycle.MutableLiveData
import com.coffenow.wave.model.DBModel

interface PlaylistService {
    var playlist: MutableLiveData<DBModel>
}