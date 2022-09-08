package com.example.musicapp.pract8

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

        val musicListRecyclerView: RecyclerView = view.findViewById(R.id.music_list_rc)
        val adapter = (requireActivity() as MainActivity).albumWiseMap[albumName]?.let {
            MusicListAdapter(
                it,
                (requireActivity() as MainActivity).mediaPlayer,
                (requireActivity() as MainActivity).menuInflater,
                0,
                musicListRecyclerView
            )
        }
        musicListRecyclerView.adapter = adapter
        musicListRecyclerView.layoutManager = LinearLayoutManager(view.context)
        val decoration: RecyclerView.ItemDecoration = DividerItemDecoration(view.context, DividerItemDecoration.HORIZONTAL)
        musicListRecyclerView.addItemDecoration(decoration)
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