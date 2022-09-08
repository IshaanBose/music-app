package com.example.musicapp

import android.animation.ValueAnimator
import android.content.Context
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import java.io.File

class MusicListAdapter(
    private val dataSet: MutableList<File>,
    private val cmp: CustomMediaPlayer,
    private val menuInflater: MenuInflater,
    var position: Int,
    recyclerView: RecyclerView
):
    RecyclerView.Adapter<MusicListAdapter.ViewHolder>() {

    private val colourFrom = ContextCompat.getColor(recyclerView.context, R.color.red)
    private val colourTo = ContextCompat.getColor(recyclerView.context, R.color.blue)
    private val colourAnimator = ValueAnimator.ofArgb(colourFrom, colourTo)
    private var isSelection = false
    private var dataRemoved = false
    val selectedItems: MutableList<Int> = ArrayList()
    var currentViewHolder: ViewHolder? = null
    var currentPosition = 0
    var isStopped = false

    var actionModeCallback: ActionMode.Callback

    init {
        cmp.mediaPlayer.setOnCompletionListener(MediaPlayer.OnCompletionListener {
            Log.d("custtest", "music complete")
            colourAnimator.cancel()
            resetCurrentDividerColour(recyclerView.context)

            currentViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_play)

            currentPosition = (currentPosition + 1) % itemCount

            if (recyclerView.findViewHolderForAdapterPosition(currentPosition) != null) {
                currentViewHolder = recyclerView.findViewHolderForAdapterPosition(currentPosition) as ViewHolder
                currentViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_pause)

                cmp.musicTitleTextView.text = currentViewHolder!!.songName.text
                cmp.musicArtistTextView.text = currentViewHolder!!.artistName.text

                resetAndStartMediaPlayer(currentPosition)
                colourAnimator.start()
            }
        })

        colourAnimator.duration = 5000
        colourAnimator.repeatCount = ValueAnimator.INFINITE
        colourAnimator.repeatMode = ValueAnimator.REVERSE
        colourAnimator.addUpdateListener( ValueAnimator.AnimatorUpdateListener {
            currentViewHolder!!.divider.setColorFilter(it.animatedValue as Int)
        } )

        cmp.musicControlButton.setOnClickListener(View.OnClickListener {
            Log.d("custtest", "in onclick ${currentPosition}")
            Log.d("custtest", if (currentViewHolder === null) "null" else "no")

            if (currentViewHolder !== null)
                musicControl(currentPosition, currentViewHolder!!)
        })

        actionModeCallback = object: ActionMode.Callback {
            override fun onCreateActionMode(mode: ActionMode, menu: Menu): Boolean {
                val inflater: MenuInflater = mode.menuInflater
                inflater.inflate(R.menu.menu_action_mode, menu)
                isSelection = true

                return true
            }

            override fun onPrepareActionMode(mode: ActionMode, menu: Menu): Boolean {
                return false
            }

            override fun onActionItemClicked(mode: ActionMode, menuItem: MenuItem): Boolean {
                selectedItems.sortDescending()

                for (i in selectedItems) {
                    dataRemoved = true
                    dataSet.removeAt(i)
                }

                mode.finish()
                return true
            }

            override fun onDestroyActionMode(mode: ActionMode) {
                isSelection = false

                Log.d("selection", selectedItems.toString())

                for (i in selectedItems) {
                    if (dataRemoved)
                        notifyItemRemoved(i)
                    else
                        notifyItemChanged(i)
                }

                dataRemoved = false
                selectedItems.clear()
            }
        }
    }

    inner class ViewHolder(
        private val menuInflater: MenuInflater,
        view: View
    ): RecyclerView.ViewHolder(view),
        View.OnCreateContextMenuListener {
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

            view.setOnCreateContextMenuListener(this)
        }

        fun selectItem(position: Int) {
            if (isSelection) {
                if (position in selectedItems) {
                    selectedItems.remove(position)
                    container.setBackgroundColor(ContextCompat.getColor(container.context,
                        R.color.transparent
                    ))
                } else {
                    selectedItems.add(position)
                    container.setBackgroundColor(ContextCompat.getColor(container.context,
                        R.color.item_select
                    ))
                }
            }
        }

        fun update(position: Int) {
            if (position in selectedItems) {
                container.setBackgroundColor(ContextCompat.getColor(container.context,
                    R.color.item_select
                ))
            } else {
                container.setBackgroundColor(ContextCompat.getColor(container.context,
                    R.color.transparent
                ))
            }

            itemView.setOnClickListener(View.OnClickListener {
                selectItem(position)
            })
        }

        override fun onCreateContextMenu(
            menu: ContextMenu?,
            v: View?,
            menuInfo: ContextMenu.ContextMenuInfo?
        ) {
            Log.d("custtest", "hiya")
            menuInflater.inflate(R.menu.menu_floating_context, menu)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_song, parent, false)

        return ViewHolder(menuInflater, view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(dataSet[position].path)

        holder.update(position)

        holder.itemView.setOnLongClickListener {
            this.position = holder.adapterPosition
            false
        }

        holder.songName.text = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE).toString()
        holder.artistName.text = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST).toString()

        holder.playButton.setOnClickListener(View.OnClickListener {
            musicControl(position, holder)
        })

        holder.seekForwardButton.setOnClickListener(View.OnClickListener {
            seek(true, 5, position)
        })

        holder.seekBackwardButton.setOnClickListener(View.OnClickListener {
            seek(false, 5, position)
        })
    }

    override fun getItemCount(): Int {
        return dataSet.size
    }

    fun musicControl(position: Int, holder: ViewHolder) {
        Log.d("custtest", position.toString())
        var startAnimation = true
        currentPosition = position
        Log.d("stoptesting", isStopped.toString())

        if (currentViewHolder === null) {
            Log.d("viewholder", "view holder null")
            currentViewHolder = holder
        }

        if (cmp.mediaPlayer.isPlaying) {
            // starting a new song when another song is already playing
            if (cmp.dataSource != dataSet[position].path.toString()) {
                colourAnimator.cancel()
                resetCurrentDividerColour(holder.divider.context)

                cmp.mediaPlayer.stop()
                currentViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_play)

                currentViewHolder = holder
                currentViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_pause)

                cmp.musicTitleTextView.text = currentViewHolder!!.songName.text
                cmp.musicArtistTextView.text = currentViewHolder!!.artistName.text

                resetAndStartMediaPlayer(position)
            } else { // trying to play song that is already playing -> pauses that song
                startAnimation = false

                cmp.mediaPlayer.pause()
                colourAnimator.pause()

                currentViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_play)
                cmp.musicControlButton.setImageResource(android.R.drawable.ic_media_play)
            }
        } else {
            // playing a song that was paused
            if (cmp.dataSource == dataSet[position].path.toString()) {
                holder.playButton.setImageResource(android.R.drawable.ic_media_pause)
                cmp.musicControlButton.setImageResource(android.R.drawable.ic_media_pause)

                if (isStopped) {
                    Log.d("stoptesting", "here")
                    cmp.mediaPlayer.prepare()
                }

                cmp.mediaPlayer.start()
                colourAnimator.resume()

                startAnimation = false
            } else { // starting a new song
                cmp.musicControlContainer.visibility = View.VISIBLE

                resetAndStartMediaPlayer(position)
                resetCurrentDividerColour(holder.divider.context) // resetting current divider

                currentViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_play)

                currentViewHolder = holder
                holder.playButton.setImageResource(android.R.drawable.ic_media_pause)

                cmp.musicControlButton.setImageResource(android.R.drawable.ic_media_pause)
                cmp.musicTitleTextView.text = currentViewHolder!!.songName.text
                cmp.musicArtistTextView.text = currentViewHolder!!.artistName.text
            }
        }

        if (startAnimation || !colourAnimator.isRunning) {
            colourAnimator.start()
        }

        isStopped = false
    }

    fun stopMusic() {
        isStopped = true
        cmp.mediaPlayer.stop()
        currentViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_play)
        cmp.musicControlButton.setImageResource(android.R.drawable.ic_media_play)
        colourAnimator.cancel()
        resetCurrentDividerColour(currentViewHolder!!.divider.context)
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

    private fun resetAndStartMediaPlayer(position: Int) {
        cmp.mediaPlayer.reset()
        cmp.mediaPlayer.setDataSource(dataSet[position].path.toString())
        cmp.dataSource = dataSet[position].path.toString()
        cmp.mediaPlayer.prepare()

        cmp.mediaPlayer.start()
    }

    private fun resetCurrentDividerColour(context: Context) {
        Log.d("custtest", "current view holder: ${ currentViewHolder!!.divider }")
        currentViewHolder!!.divider.setColorFilter(ContextCompat.getColor(
            context,
            if (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK == Configuration.UI_MODE_NIGHT_YES)
                R.color.white
            else
                R.color.black
        ))
    }
}