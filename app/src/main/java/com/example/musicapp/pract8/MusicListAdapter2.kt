package com.example.musicapp.pract8

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MusicListAdapter2(
    private val dataSet: List<File>,
    private val cmp: CustomMediaPlayer,
    private val context: Context,
    var position: Int
): RecyclerView.Adapter<MusicListAdapter2.ViewHolder>() {
    private val colourFrom = ContextCompat.getColor(context, R.color.red)
    private val colourTo = ContextCompat.getColor(context, R.color.blue)
    private val colourAnimator = ValueAnimator.ofArgb(colourFrom, colourTo)
    var currentViewHolder: MusicListAdapter.ViewHolder? = null
    var currentPosition = 0
    var isStopped = false

    init {
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view) {
        val songName: TextView; val artistName: TextView;
        val playButton: ImageView;
        val seekForwardButton: ImageView; val seekBackwardButton: ImageView;
        val divider: ImageView
        val container: LinearLayoutCompat

        init {
            songName = view.findViewById(R.id.song_name)
            artistName = view.findViewById(R.id.artist_name)
            playButton = view.findViewById(R.id.play_button)
            divider = view.findViewById(R.id.divider)
            seekForwardButton = view.findViewById(R.id.seek_forward)
            seekBackwardButton = view.findViewById(R.id.seek_backward)
            container = view.findViewById(R.id.view_holder_container)

            playButton.setOnClickListener(View.OnClickListener {
                val position = adapterPosition


            })
        }
    }

    private fun resetAndStartMediaPlayer(position: Int) {
        cmp.mediaPlayer.reset()
        cmp.mediaPlayer.setDataSource(dataSet[position].path.toString())
        cmp.dataSource = dataSet[position].path.toString()
        cmp.mediaPlayer.prepare()

        cmp.mediaPlayer.start()
    }

    private fun resetCurrentDividerColour(context: Context) {
        currentViewHolder!!.divider.setColorFilter(ContextCompat.getColor(
            context,
            if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
                R.color.white
            else
                R.color.black
        ))
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}