package com.example.musicapp

import android.content.DialogInterface
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import androidx.appcompat.app.AppCompatActivity
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.PopupMenu
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.jar.Manifest

class MainActivity : AppCompatActivity() {
    lateinit var files: Array<File>
    lateinit var adapter: MusicListAdapter
//    lateinit var chooseDirButton: Button
//    lateinit var musicList: RecyclerView
    lateinit var mediaPlayer: CustomMediaPlayer
//    lateinit var chooseOtherDirButton: Button
    lateinit var musicPlayerContainer: LinearLayoutCompat

    private val POPUP_MENU_FIRST = 100
    private val SP_FILE = "com.example.musicapp.dirs"
    private val DIR_SEPARATOR = "<>"

    private val folder = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri == null) {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("Error!")
                .setMessage("Please choose a directory!")
                .setNeutralButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            val file = uri.path?.let { File(it) }
            val pathWithoutExt = file!!.path.split("primary:")[1]
            createMusicList(pathWithoutExt)
        }
    }

    private val requestPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
        if (isGranted) {
            folder.launch(null)
        } else {
            Toast.makeText(baseContext, "Permission denied!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        musicList = findViewById(R.id.song_list)
        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, ButtonFragment.newInstance("", ""))
            }
        }
//        chooseDirButton = findViewById(R.id.dir_select)
        musicPlayerContainer = findViewById(R.id.music_player_container)

//        registerForContextMenu(musicList)

        mediaPlayer = CustomMediaPlayer(
            "",
            MediaPlayer.create(baseContext, R.raw.instrumental),
            findViewById(R.id.music_title),
            findViewById(R.id.music_artist),
            findViewById(R.id.music_control),
            musicPlayerContainer
        )

        mediaPlayer.musicTitleTextView.isSelected = true

//        chooseDirButton.setOnClickListener(View.OnClickListener {
//            requestPermission()
//        })
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    baseContext,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    folder.launch(null)
                }

                shouldShowRequestPermissionRationale(android.Manifest.permission.READ_EXTERNAL_STORAGE) -> {
                    MaterialAlertDialogBuilder(this@MainActivity)
                        .setTitle("Permission Required!")
                        .setMessage("This app needs to be able to read external storage in order to play mp3 files.")
                        .setPositiveButton("OK") { dialog, _ ->
                            dialog.dismiss()
                            requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                        }
                        .setNegativeButton("Cancel") { dialog, _ ->
                            dialog.dismiss()
                            Toast.makeText(baseContext, "Permission denied!", Toast.LENGTH_LONG).show()
                        }
                        .show()
                }

                else -> {
                    requestPermissionLauncher.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }
    }

    private fun createMusicList(pathWithoutExt: String) {
        val path = "${Environment.getExternalStorageDirectory()}/$pathWithoutExt"
        Log.d("custtest", path)

        val parsedUri = Uri.parse(path)
        val file = File(parsedUri.path.toString())

        files = cleanFilesList(file.listFiles()!!)

        if (files.isEmpty()) {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("Error!")
                .setMessage("The chosen directory is either empty, or does not contain any mp3/mp4 files!")
                .setNeutralButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .setCancelable(false)
                .show()
        } else {
            val sp = getSharedPreferences(SP_FILE, MODE_PRIVATE)
            val dirs = sp.getString("dirs", null)
            val dirList: MutableList<String>
            Log.d("custtest", "dirs: ${dirs.toString()}")

            if (dirs !== null) {
                dirList = dirs.split(DIR_SEPARATOR).toMutableList()

                if (dirList.size == 5) {
                    dirList.removeLast()
                }

                Log.d("custtest", dirList.toString())

                if (pathWithoutExt in dirList) {
                    dirList.removeAt(dirList.indexOf(pathWithoutExt))
                }

                dirList.add(0, pathWithoutExt)
            } else {
                dirList = mutableListOf(pathWithoutExt)
            }

            val spEditor = getSharedPreferences(SP_FILE, MODE_PRIVATE).edit()
            spEditor.putString("dirs", dirList.joinToString(DIR_SEPARATOR))
            spEditor.apply()

            if (mediaPlayer.mediaPlayer.isPlaying) {
                mediaPlayer.mediaPlayer.stop()
                mediaPlayer.mediaPlayer.reset()
            }

//            adapter = MusicListAdapter(files.toMutableList(), mediaPlayer, menuInflater, 0, musicList)
//
//            musicList.adapter = adapter
//            musicList.layoutManager = LinearLayoutManager(applicationContext)
//            val decoration: RecyclerView.ItemDecoration = DividerItemDecoration(baseContext, DividerItemDecoration.HORIZONTAL)
//            musicList.addItemDecoration(decoration)
//
//            chooseDirButton.visibility = View.GONE
//            musicList.visibility = View.VISIBLE
//            musicPlayerContainer.visibility = View.GONE
        }
    }

    private fun cleanFilesList(files: Array<File>): Array<File> {
        val mutableList = mutableListOf<File>()
        var extension: String

        for (i in files.indices) {
            Log.d("custtest", files[i].name)
            if (files[i].isFile && files[i].name.contains(".")) {
                extension = files[i].name.substring(files[i].name.lastIndexOf("."))

                if (extension == ".mp3" || extension == ".mp4")
                    mutableList.add(files[i])
            }
        }

        return mutableList.toTypedArray()
    }

//    override fun onCreateOptionsMenu(menu: Menu): Boolean {
//        val inflater: MenuInflater = menuInflater
//        var openDirIcon = 0
//        var closedDirIcon = 0
//
//        when (baseContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
//            Configuration.UI_MODE_NIGHT_YES -> {
//                inflater.inflate(R.menu.menu_main_dark, menu)
//                openDirIcon = R.drawable.ic_directory_open_white
//                closedDirIcon = R.drawable.ic_directory_white
//            }
//            Configuration.UI_MODE_NIGHT_NO -> {
//                inflater.inflate(R.menu.menu_main_light, menu)
//                openDirIcon = R.drawable.ic_directory_open_black
//                closedDirIcon = R.drawable.ic_directory_black
//            }
//        }
//
//        Handler(Looper.myLooper()!!).post(Runnable {
//            val view = findViewById<View>(R.id.choose_dir)
//
//            if (view !== null) {
//                view.setOnLongClickListener(View.OnLongClickListener {
//                    menu.getItem(1).setIcon(openDirIcon)
//
//                    val popupMenu = PopupMenu(baseContext, it)
//                    val sp = getSharedPreferences(SP_FILE, MODE_PRIVATE)
//                    val dirs = sp.getString("dirs", null)
//
//                    if (dirs !== null) {
//                        val dirList = dirs.split(DIR_SEPARATOR)
//
//                        for (i in dirList.indices) {
//                            popupMenu.menu.add(0, POPUP_MENU_FIRST + i, i, dirList[i])
//                                .setIcon(R.drawable.ic_directory_open_black)
//                                .setOnMenuItemClickListener {
//                                    createMusicList(dirList[i])
//                                    true
//                                }
//                                .setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT)
//
//                            popupMenu.setOnDismissListener {
//                                menu.getItem(1).setIcon(closedDirIcon)
//                            }
//                        }
//
//                        popupMenu.show()
//                    } else {
//                        Toast.makeText(baseContext, "No recent directories!", Toast.LENGTH_LONG).show()
//                    }
//
//                    true
//                })
//            }
//        })
//
//        return super.onCreateOptionsMenu(menu)
//    }

//    override fun onOptionsItemSelected(item: MenuItem): Boolean {
//        return when (item.itemId) {
//            R.id.choose_dir -> {
//                requestPermission()
//                true
//            }
//
//            R.id.select -> {
//                startActionMode(adapter.actionModeCallback)
//                true
//            }
//
//            else -> super.onOptionsItemSelected(item)
//        }
//    }

    override fun onDestroy() {
        super.onDestroy()

        if (mediaPlayer.mediaPlayer.isPlaying) {
            mediaPlayer.mediaPlayer.stop()
        }

        mediaPlayer.mediaPlayer.release()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
        val position = adapter.position
//        val viewHolder = musicList.findViewHolderForAdapterPosition(position)

//        when (item.itemId) {
//            R.id.music_control -> adapter.musicControl(position, viewHolder as MusicListAdapter.ViewHolder)
//            R.id.music_stop -> adapter.stopMusic()
//            R.id.seek_forward_5 -> adapter.seek(true, 5, position)
//            R.id.seek_forward_10 -> adapter.seek(true, 10, position)
//            R.id.seek_backward_5 -> adapter.seek(false, 5, position)
//            R.id.seek_backward_10 -> adapter.seek(false, 10, position)
//        }

        return super.onContextItemSelected(item)
    }
}