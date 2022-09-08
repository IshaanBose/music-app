package com.example.musicapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView

class AlbumListAdapter(
    private val dataSet: List<String>,
    private val activity: MainActivity
): RecyclerView.Adapter<AlbumListAdapter.ViewHolder>() {
    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val albumName: TextView
        val album: CardView

        init {
            albumName = view.findViewById(R.id.album_name)
            album = view.findViewById(R.id.album)

            album.setOnClickListener(View.OnClickListener {
                val position = adapterPosition
                activity.switchToMusicList(dataSet[position])
                activity.currentAlbum = dataSet[position]
            })
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_album, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.albumName.text = dataSet[position]
//        holder.album.setOnClickListener(View.OnClickListener {
//            activity.switchToMusicList(dataSet[position])
//        })
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}