package com.example.musicapp

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MusicListAdapter(
    private val dataSet: List<File>,
    private val cmp: CustomMediaPlayer,
    private val activity: MainActivity,
    val album: String,
    var position: Int
): RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {
    private val colourFrom = ContextCompat.getColor(activity.baseContext, R.color.red)
    private val colourTo = ContextCompat.getColor(activity.baseContext, R.color.blue)
    val colourAnimator = ValueAnimator.ofArgb(colourFrom, colourTo)
    var isStopped = false

    init {
        colourAnimator.duration = 5000
        colourAnimator.repeatCount = ValueAnimator.INFINITE
        colourAnimator.repeatMode = ValueAnimator.REVERSE
        colourAnimator.addUpdateListener( ValueAnimator.AnimatorUpdateListener {
            if (activity.currentSongViewHolder !== null) {
                activity.currentSongViewHolder!!.divider.setColorFilter(it.animatedValue as Int)
            }
        } )
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

                activity.musicControl(
                    position,
                    this,
                    dataSet,
                    this@MusicListAdapter,
                    album
                )
            })
        }
    }

    fun resetCurrentDividerColour(context: Context) {
        activity.currentSongViewHolder!!.divider.setColorFilter(ContextCompat.getColor(
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
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(dataSet[position].path)

        holder.songName.text = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toString()
        holder.artistName.text = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).toString()

        if (cmp.dataSource == dataSet[position].path.toString()) {
            activity.currentSongViewHolder = holder

            if (cmp.mediaPlayer.isPlaying) {
                Log.d("on_bind", "playing already")
                holder.playButton.setImageResource(android.R.drawable.ic_media_pause)

                if (!colourAnimator.isRunning)
                    colourAnimator.start()
            }
        } else {
            holder.playButton.setImageResource(android.R.drawable.ic_media_play)
        }
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }
}