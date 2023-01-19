package com.coffenow.wave.ui.popUps

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.coffenow.wave.R
import com.coffenow.wave.databinding.FragmentCreatePlaylistBinding
import com.coffenow.wave.utils.WaveDBHelper

class CreatePlaylist : Fragment() {
    private var _binding: FragmentCreatePlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var appContext : Context
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db: SQLiteDatabase
    private lateinit var saveBtn : Button
    private lateinit var inputEditText: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setBinding()
    }

    private fun setBinding() {
        appContext= requireContext().applicationContext
        dbHelper = WaveDBHelper(appContext)
        saveBtn = binding.saveBtn
        inputEditText = binding.playlistName
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentCreatePlaylistBinding.inflate(inflater, container, false)
        return binding.root
        }

    private fun createList() {
        db = dbHelper.readableDatabase
        saveBtn.setOnClickListener{
            if(inputEditText.text.isNotEmpty()){
                val playlistName: String = inputEditText.text.toString()
                dbHelper.createPlaylist(playlistName)
                val data = ContentValues()
                data.put("title", playlistName)
                db.insert("playlists", null, data)
                inputEditText.setText("")
                inputEditText.hint = "Name"
            } else{
                Toast.makeText(appContext, R.string.isEmpty, Toast.LENGTH_SHORT).show()
            }
        }
    }

}