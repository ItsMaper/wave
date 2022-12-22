package com.coffenow.wave.adapter

import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.coffenow.wave.activities.PlayerActivity
import com.coffenow.wave.databinding.LocalMusicBinding
import com.coffenow.wave.model.LocalModel

class LocalMusicAdapter: RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private var localItems = getLocalList()
    private val context: Context? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LocalMusicBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LocalHolder(view)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as LocalHolder).setData(localItems[position])
    }

    class LocalHolder(itemView: LocalMusicBinding) : RecyclerView.ViewHolder(itemView.root) {
        private val binding = itemView

        fun setData(data: LocalModel){
            binding.root.setOnClickListener {
                val i = Intent(it.context, PlayerActivity::class.java)
                i.putExtra("type", "local")
                i.putExtra("uri", data.uri)
                i.putExtra("title", data.title)

                it.context.startActivity(i)
            }
            binding.localTitle.text = data.title
        }
    }

    override fun getItemCount(): Int = localItems.size

    private fun getLocalList(): ArrayList<LocalModel> {
        val list = ArrayList<LocalModel>()
        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                ) } else { MediaStore.Audio.Media.EXTERNAL_CONTENT_URI }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.DATA)
        val selection = MediaStore.Audio.Media.IS_MUSIC + "!= 0"
        val sortOrder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"

        val query = context?.contentResolver?.query(
            collection,
            projection,
            selection, null,
            sortOrder
        )?.use { cursor ->
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)

            while (cursor.moveToNext()){
                val idC = cursor.getLong(idColumn)
                val titleC = cursor.getString(nameColumn)
                val contentUri: Uri = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, idC)
                list.add(LocalModel(id = idC, title = titleC, uri = contentUri))
            }
            println(list)
        }
        return list}

}