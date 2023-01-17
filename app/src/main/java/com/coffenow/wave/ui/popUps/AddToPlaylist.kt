package com.coffenow.wave.ui.popUps

import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.coffenow.wave.R
import com.coffenow.wave.adapter.RecyclerPlaylistByDB
import com.coffenow.wave.databinding.FragmentAddToPlaylistBinding
import com.coffenow.wave.db.WaveDBHelper

class AddToPlaylist(context: Context) : DialogFragment() {
    private var _binding: FragmentAddToPlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db: SQLiteDatabase
    private var appContext = context

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        this.dialog?.setTitle(resources.getString(R.string.SelectPlaylist))
        setBind()
        initRecyclerView() }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        _binding = FragmentAddToPlaylistBinding.inflate(inflater, container, false)
        return inflater.inflate(R.layout.fragment_add_to_playlist, container, false) }

    private fun setBind() {
        appContext= requireContext().applicationContext
        dbHelper = WaveDBHelper(appContext)
        binding.addBtn.setOnClickListener {
            dismiss()
        }
    }

    private fun initRecyclerView() {
        db = dbHelper.readableDatabase
        val dbAdapter = RecyclerPlaylistByDB()
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM playlists",null
        )
        dbAdapter.rvSet(appContext, cursor)
        val manager = LinearLayoutManager(requireContext())
        binding.lvPlaylists.apply {
            layoutManager = manager
            adapter = dbAdapter
        }
    }
}