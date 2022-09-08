package com.example.musicapp.pract8

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView

class AlbumViewFragment : Fragment() {
    private val GRID_ITEM_WIDTH = 150f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_album_view, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val albumRecyclerView: RecyclerView = view.findViewById(R.id.album_rc)
        val adapter = AlbumListAdapter(
            (requireActivity() as MainActivity).albumWiseMap.keys.toList(),
            (requireActivity() as MainActivity)
        )

        albumRecyclerView.layoutManager = GridLayoutManager(view.context, 2)

        albumRecyclerView.adapter = adapter
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            AlbumViewFragment().apply {}
    }
}