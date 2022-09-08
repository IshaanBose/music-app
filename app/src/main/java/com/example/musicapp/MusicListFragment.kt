package com.example.musicapp

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

private const val ARG_PARAM1 = "album"

class MusicListFragment : Fragment() {
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
        val musicListRecyclerView: RecyclerView = view.findViewById(R.id.music_list_rc)
        val adapter = activity.albumWiseMap[albumName]?.let {
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

        activity.mediaPlayer.mediaPlayer.setOnCompletionListener {
            activity.currentSongPosition =
                (activity.currentSongPosition + 1) % (activity.albumWiseMap[activity.currentAlbum]?.size ?: 1)
            adapter!!.colourAnimator.cancel()
            adapter.resetCurrentDividerColour(activity.baseContext)
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
                adapter.colourAnimator.start()
            }
        }
    }

    override fun onStop() {
        super.onStop()

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