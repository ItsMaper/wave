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
import com.coffenow.wave.adapter.PlaylistsByDB
import com.coffenow.wave.databinding.FragmentAddToPlaylistBinding
import com.coffenow.wave.model.DBPlaylistModel
import com.coffenow.wave.utils.WaveDBHelper

class AddToPlaylistFragment(context: Context) : DialogFragment() {
    private var _binding: FragmentAddToPlaylistBinding? = null
    private val binding get() = _binding!!
    private lateinit var dbHelper: WaveDBHelper
    private lateinit var db: SQLiteDatabase
    private var appContext = context



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
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
        val dbAdapter = PlaylistsByDB()
        val cursor: Cursor = db.rawQuery(
            "SELECT * FROM playlists",null
        )
        val items = getData(cursor)
        val manager = LinearLayoutManager(requireContext())
        binding.lvPlaylists.apply {
            layoutManager = manager
            adapter = dbAdapter
        }
        dbAdapter.rvSet(appContext, true, items)
    }

    private fun getData(cursor: Cursor): ArrayList<DBPlaylistModel> {
        val items =ArrayList<DBPlaylistModel>()
        var i = 0
        if (cursor.count == 1){
            cursor.moveToFirst()
            val title = cursor.getString(0)
            items.add(DBPlaylistModel(title))
        } else{
            if (cursor.count  != 0){
                while (i < cursor.count-1){
                    cursor.moveToPosition(i)
                    val title = cursor.getString(0)
                    items.add(DBPlaylistModel(title))
                    i++
                }
            }
        }


        return items
    }
}