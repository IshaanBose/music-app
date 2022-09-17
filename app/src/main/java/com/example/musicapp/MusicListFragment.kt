package com.example.musicapp

import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val ARG_PARAM1 = "album"

class MusicListFragment : Fragment() {
    lateinit var musicListRecyclerView: RecyclerView
    private var adapter: MusicListAdapter? = null
    private var albumName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            albumName = it.getString(ARG_PARAM1)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_music_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = (requireActivity() as MainActivity)
        musicListRecyclerView = view.findViewById(R.id.music_list_rc)
        adapter = activity.albumWiseMap[albumName]?.let {
            MusicListAdapter(
                it,
                activity.mediaPlayer,
                activity,
                albumName!!,
                0
            )
        }

        activity.currentAlbum = albumName
        activity.adapter = adapter

        musicListRecyclerView.adapter = adapter
        musicListRecyclerView.layoutManager = LinearLayoutManager(view.context)
        val decoration: RecyclerView.ItemDecoration = DividerItemDecoration(view.context, DividerItemDecoration.HORIZONTAL)
        musicListRecyclerView.addItemDecoration(decoration)

        registerForContextMenu(musicListRecyclerView)

        activity.mediaPlayer.mediaPlayer.setOnCompletionListener {
            activity.currentSongPosition =
                (activity.currentSongPosition + 1) % (activity.albumWiseMap[activity.currentAlbum]?.size ?: 1)
            Log.d("completed_song", activity.currentSongPosition.toString())
            adapter!!.colourAnimator.cancel()
            adapter!!.resetCurrentDividerColour(activity.baseContext)
            activity.currentSongViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_play)

            if (musicListRecyclerView.findViewHolderForAdapterPosition(activity.currentSongPosition) != null) {
                activity.currentSongViewHolder = musicListRecyclerView.findViewHolderForAdapterPosition(activity.currentSongPosition) as MusicListAdapter.ViewHolder
                activity.currentSongViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_pause)
                activity.updateBottomMusicPlayer(
                    activity.currentSongViewHolder!!.artistName.text.toString(),
                    activity.currentSongViewHolder!!.songName.text.toString(),
                    true
                )
                activity.resetAndStartMediaPlayer(albumName!!, activity.currentSongPosition)
                adapter!!.colourAnimator.start()
            }
        }
    }

//    override fun onCreateContextMenu(
//        menu: ContextMenu,
//        v: View,
//        menuInfo: ContextMenu.ContextMenuInfo?
//    ) {
//        super.onCreateContextMenu(menu, v, menuInfo)
//        val inflater: MenuInflater = requireActivity().menuInflater
//        inflater.inflate(R.menu.menu_floating_context, menu)
//    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = adapter!!.position
        val viewHolder = musicListRecyclerView.findViewHolderForAdapterPosition(position)
        val activity = requireActivity() as MainActivity

        when (item.itemId) {
            R.id.music_control -> activity
                .musicControl(
                    position,
                    viewHolder as MusicListAdapter.ViewHolder,
                    activity.albumWiseMap[albumName]!!,
                    adapter,
                    albumName!!
                )
            R.id.music_stop -> adapter!!.stopMusic()
            R.id.seek_forward_5 -> adapter!!.seek(true, 5, position)
            R.id.seek_forward_10 -> adapter!!.seek(true, 10, position)
            R.id.seek_backward_5 -> adapter!!.seek(false, 5, position)
            R.id.seek_backward_10 -> adapter!!.seek(false, 10, position)
        }

        return super.onContextItemSelected(item)
    }

    override fun onPause() {
        super.onPause()

        val activity = (requireActivity() as MainActivity)

        activity.adapter?.colourAnimator?.cancel()
        activity.adapter = null
        activity.mediaPlayer.mediaPlayer.setOnCompletionListener {
            activity.onCompleteWithoutMusicListListener()
        }
    }

    companion object {
        @JvmStatic fun newInstance(albumName: String) =
                MusicListFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_PARAM1, albumName)
                    }
                }
    }
}