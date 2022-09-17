package com.example.musicapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.LinearLayoutCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

class SettingsFragment : Fragment() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val activity = requireActivity() as MainActivity
        val sp = activity.getSharedPreferences("${activity.spFile}.settings", Context.MODE_PRIVATE)
        val defaultSeekIntervalContainer: LinearLayoutCompat = view.findViewById(R.id.default_seek_interval)
        val maxRecentDirs: LinearLayoutCompat = view.findViewById(R.id.max_recent_dirs)
        val resetSettings: LinearLayoutCompat = view.findViewById(R.id.reset_settings)
        val logout: LinearLayoutCompat = view.findViewById(R.id.log_out)
        val deleteAccount: LinearLayoutCompat = view.findViewById(R.id.delete_account)
        val currentIntervalTextView: TextView = view.findViewById(R.id.current_interval)
        val currentMaxDirsTextView: TextView = view.findViewById(R.id.current_max_dirs)

        var currentDefSeekInt = sp.getInt(SP_DEF_SEEK_KEY, 5)
        val defSeekIntChoices = arrayOf("5s", "10s", "15s", "20s", "25s", "30s")

        var currentMaxRecDirs = sp.getInt(SP_MAX_REC_DIRS_KEY, 5)
        val maxRecDirsChoices = arrayOf("2", "3", "4", "5", "6", "7", "8", "9", "10")

        currentIntervalTextView.text =
            resources.getString(R.string.current_interval, defSeekIntChoices[defSeekIntChoices.indexOf("${currentDefSeekInt}s")])
        currentMaxDirsTextView.text =
            resources.getString(R.string.current_max_rec_dirs, maxRecDirsChoices[maxRecDirsChoices.indexOf("$currentMaxRecDirs")])

        defaultSeekIntervalContainer.setOnClickListener(View.OnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Default Seek Interval")
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("OK") { dialog, _ ->
                    val spEditor = sp.edit()
                    spEditor.putInt(SP_DEF_SEEK_KEY, currentDefSeekInt)
                    spEditor.apply()

                    currentIntervalTextView.text = resources.getString(R.string.current_interval,
                        defSeekIntChoices[defSeekIntChoices.indexOf("${currentDefSeekInt}s")])

                    dialog.dismiss()
                }
                .setSingleChoiceItems(
                    defSeekIntChoices,
                    (currentDefSeekInt / 5) - 1
                ) { dialog, which ->
                    currentDefSeekInt = defSeekIntChoices[which].split("s")[0].toInt()
                }
                .show()
        })

        maxRecentDirs.setOnClickListener(View.OnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Max Recent Directories")
                .setNeutralButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .setPositiveButton("OK") { dialog, _ ->
                    val spEditor = sp.edit()
                    spEditor.putInt(SP_MAX_REC_DIRS_KEY, currentMaxRecDirs)
                    spEditor.apply()

                    val spDir = activity.getSharedPreferences("${activity.spFile}.dirs", AppCompatActivity.MODE_PRIVATE)
                    val dirs = spDir.getString("dirs", null)

                    if (dirs !== null) {
                        val dirList = dirs.split(activity.DIR_SEPARATOR).toMutableList()

                        while (dirList.size > currentMaxRecDirs) {
                            dirList.removeLast()
                        }

                        val spDirEditor = spDir.edit()
                        spDirEditor.putString("dirs", dirList.joinToString(activity.DIR_SEPARATOR))
                        spDirEditor.apply()
                    }

                    currentMaxDirsTextView.text =
                        resources.getString(R.string.current_max_rec_dirs,
                            maxRecDirsChoices[maxRecDirsChoices.indexOf("$currentMaxRecDirs")])

                    dialog.dismiss()
                }
                .setSingleChoiceItems(
                    maxRecDirsChoices,
                    maxRecDirsChoices.indexOf(currentMaxRecDirs.toString())
                ) { dialog, which ->
                    currentMaxRecDirs = maxRecDirsChoices[which].toInt()
                }
                .show()
        })

        resetSettings.setOnClickListener(View.OnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Settings")
                .setMessage("This is reset settings to default values. Are you sure you want to proceed?")
                .setPositiveButton("Yes") { dialog, _ ->
                    val spEditor = sp.edit()
                    spEditor.remove(SP_DEF_SEEK_KEY)
                    spEditor.remove(SP_MAX_REC_DIRS_KEY)
                    spEditor.apply()

                    currentMaxRecDirs = 5
                    currentDefSeekInt = 5

                    currentIntervalTextView.text =
                        resources.getString(R.string.current_interval, defSeekIntChoices[defSeekIntChoices.indexOf("${currentDefSeekInt}s")])
                    currentMaxDirsTextView.text =
                        resources.getString(R.string.current_max_rec_dirs, maxRecDirsChoices[maxRecDirsChoices.indexOf("$currentMaxRecDirs")])

                    dialog.dismiss()
                }
                .setNegativeButton("No") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        })

        logout.setOnClickListener(View.OnClickListener {
            activity.logout()
        })

        deleteAccount.setOnClickListener(View.OnClickListener {
            MaterialAlertDialogBuilder(requireContext())
                .setTitle("Reset Settings")
                .setMessage(resources.getString(R.string.delete_account_msg))
                .setPositiveButton("OK") { dialog, _ ->
                    val spUsersEditor = activity.getSharedPreferences("com.musicapp.user_details", Context.MODE_PRIVATE).edit()
                    spUsersEditor.remove(activity.username)
                    spUsersEditor.apply()
                    dialog.dismiss()
                    activity.logout()
                }
                .setNegativeButton("Cancel") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        })
    }

    companion object {
        @JvmStatic
        val SP_DEF_SEEK_KEY = "defSeek"
        @JvmStatic
        val SP_MAX_REC_DIRS_KEY = "maxRecDirs"
        @JvmStatic
        fun newInstance() =
            SettingsFragment().apply {}
    }
}