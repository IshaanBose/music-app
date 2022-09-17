package com.example.musicapp

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.util.Log
import android.view.*
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
                Log.d("animator", activity.currentSongViewHolder!!.songName.text.toString())
                activity.currentSongViewHolder!!.divider.setColorFilter(it.animatedValue as Int)
            }
        })
    }

    inner class ViewHolder(view: View): RecyclerView.ViewHolder(view), View.OnCreateContextMenuListener {
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

            val spSettings = activity.getSharedPreferences("${activity.spFile}.settings", Context.MODE_PRIVATE)

            Log.d("view_holder", "Init: $adapterPosition")

            playButton.setOnClickListener(View.OnClickListener {
                val position = adapterPosition
                Log.d("view_holder", "In onclick: $position")

                activity.musicControl(
                    position,
                    this,
                    dataSet,
                    this@MusicListAdapter,
                    album
                )
            })

            seekForwardButton.setOnClickListener(View.OnClickListener {
                val position = adapterPosition
                seek(true, spSettings.getInt(SettingsFragment.SP_DEF_SEEK_KEY, 5), position)
            })

            seekBackwardButton.setOnClickListener(View.OnClickListener {
                val position = adapterPosition
                seek(false, spSettings.getInt(SettingsFragment.SP_DEF_SEEK_KEY, 5), position)
            })

            view.setOnCreateContextMenuListener(this)
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            view: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            val inflater: MenuInflater = activity.menuInflater
            inflater.inflate(R.menu.menu_floating_context, menu)
        }
    }

    fun stopMusic() {
        activity.isStopped = true
        cmp.mediaPlayer.stop()
        activity.currentSongViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_play)
        cmp.musicControlButton.setImageResource(android.R.drawable.ic_media_play)
        colourAnimator.cancel()
        resetCurrentDividerColour(activity.currentSongViewHolder!!.divider.context)
    }

    fun seek(forward: Boolean, interval: Int, position: Int) {
        if (cmp.mediaPlayer.isPlaying) {
            if (cmp.dataSource == dataSet[position].path.toString()) {
                val newTime = cmp.mediaPlayer.currentPosition +
                        if (forward) (interval * 1000) else -(interval * 1000)

                if (newTime < 0) {
                    cmp.mediaPlayer.seekTo(0)
                } else if (newTime > cmp.mediaPlayer.duration) {
                    cmp.mediaPlayer.seekTo(cmp.mediaPlayer.duration)
                } else {
                    cmp.mediaPlayer.seekTo(newTime)
                }
            }
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
        holder.itemView.setOnLongClickListener(View.OnLongClickListener {
            Log.d("context_menu", "in onLongClick")
            this.position = holder.adapterPosition
            false
        })

        if (cmp.dataSource == dataSet[position].path.toString()) {
            Log.d("on_bind", "Setting current view holder as ${holder.songName.text}")
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