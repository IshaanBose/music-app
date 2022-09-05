package com.example.musicapp.pract8

import android.content.res.Configuration
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import com.google.android.material.button.MaterialButton

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [ButtonFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
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

        val menuHost: MenuHost = requireActivity()

        view.findViewById<MaterialButton>(R.id.choose_dir).setOnClickListener(View.OnClickListener {
            (requireActivity() as MainActivity).requestPermission()
        })

        menuHost.addMenuProvider(object: MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                var openDirIcon = 0
                var closedDirIcon = 0

                when (context!!.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
                    Configuration.UI_MODE_NIGHT_YES -> {
                        menuInflater.inflate(R.menu.menu_main_dark, menu)
                        openDirIcon = R.drawable.ic_directory_open_white
                        closedDirIcon = R.drawable.ic_directory_white
                    }
                    Configuration.UI_MODE_NIGHT_NO -> {
                        menuInflater.inflate(R.menu.menu_main_light, menu)
                        openDirIcon = R.drawable.ic_directory_open_black
                        closedDirIcon = R.drawable.ic_directory_black
                    }
                }

                menu.getItem(0).setVisible(false)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                TODO("Not yet implemented")
            }

        })
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment ButtonFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            ButtonFragment().apply {
//                arguments = Bundle().apply {
//                    putString(ARG_PARAM1, param1)
//                    putString(ARG_PARAM2, param2)
//                }
            }
    }
}