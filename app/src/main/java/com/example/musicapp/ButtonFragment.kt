package com.example.musicapp

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment

import com.google.android.material.button.MaterialButton

class ButtonFragment : Fragment() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_button, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.findViewById<MaterialButton>(R.id.choose_dir).setOnClickListener(View.OnClickListener {
            (requireActivity() as MainActivity).requestPermission()
        })
    }

    companion object {
        @JvmStatic
        fun newInstance() =
            ButtonFragment().apply {}
    }
}