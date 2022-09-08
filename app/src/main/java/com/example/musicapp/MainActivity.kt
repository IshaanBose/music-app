package com.example.musicapp

import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.File
import java.util.*

class MainActivity : AppCompatActivity() {
    lateinit var files: MutableList<File>
    lateinit var mediaPlayer: CustomMediaPlayer
    lateinit var musicPlayerContainer: LinearLayoutCompat
    lateinit var albumWiseMap: TreeMap<String, MutableList<File>>
    var currentAlbum: String? = null
    var currentSongPosition: Int = 0
    var adapter: MusicListAdapter? = null
    var currentSongViewHolder: MusicListAdapter.ViewHolder? = null

    private val POPUP_MENU_FIRST = 100
    private val SP_FILE = "com.example.musicapp.dirs"
    private val DIR_SEPARATOR = "<>"
    private val EXTENSIONS = listOf(
        ".mp3", ".m4a", ".m4p", ".ogg", ".wav"
    )

    private val folderSelector = registerForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri: Uri? ->
        if (uri == null) {
            MaterialAlertDialogBuilder(this@MainActivity)
                .setTitle("Error!")
                .setMessage("Please choose a directory!")
                .setNeutralButton("OK") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        } else {
            val selectedFile = uri.path?.let { File(it) }
            val pathWithoutExt = selectedFile!!.path.split("primary:")[1]
            val path = "${Environment.getExternalStorageDirectory()}/$pathWithoutExt"
            val parsedUri = Uri.parse(path)
            val file = File(parsedUri.path.toString())
            files = mutableListOf()

            findMusicFiles(file)
            albumWiseMap = getAlbumWiseSongs()
            supportFragmentManager.commit {
                setCustomAnimations(
                    R.anim.fade_in,
                    R.anim.fade_out,
                    R.anim.fade_in,
                    R.anim.fade_out
                )
                setReorderingAllowed(true)
                replace(R.id.fragment_container, AlbumViewFragment.newInstance())
            }
        }
    }

    private val requestPermissionLauncher: ActivityResultLauncher<String> = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
            isGranted: Boolean ->
        if (isGranted) {
            folderSelector.launch(null)
        } else {
            Toast.makeText(baseContext, "Permission denied!", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                setReorderingAllowed(true)
                add(R.id.fragment_container, ButtonFragment.newInstance())
            }
        }

        musicPlayerContainer = findViewById(R.id.music_player_container)

        mediaPlayer = CustomMediaPlayer(
            "",
            MediaPlayer.create(baseContext, R.raw.instrumental),
            findViewById(R.id.music_title),
            findViewById(R.id.music_artist),
            findViewById(R.id.music_control),
            musicPlayerContainer
        )

        mediaPlayer.musicTitleTextView.isSelected = true
        mediaPlayer.musicControlButton.setOnClickListener(View.OnClickListener {
            musicControl(
                currentSongPosition,
                currentSongViewHolder,
                albumWiseMap.get(currentAlbum)!!,
                adapter,
                currentAlbum!!
            )
        })
    }

    fun musicControl(
        position: Int,
        holder: MusicListAdapter.ViewHolder?,
        dataSet: List<File>,
        adapter: MusicListAdapter?,
        album: String
    ) {
        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(dataSet[position].path)
        val songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE) ?: "NULL"
        val artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST) ?: "NULL"
        
        if (adapter !== null) {
            var startAnimation = true

            currentSongPosition = position

            if (currentSongViewHolder === null) {
                currentSongViewHolder = holder
            }

            if (mediaPlayer.mediaPlayer.isPlaying) {
                // starting a new song when another song is already playing
                if (mediaPlayer.dataSource != dataSet[position].path.toString()) {
                    adapter.colourAnimator.cancel()
                    adapter.resetCurrentDividerColour(baseContext)

                    mediaPlayer.mediaPlayer.stop()
                    currentSongViewHolder!!.playButton.setImageResource(android.R.drawable.ic_media_play)

                    currentSongViewHolder = holder
                    holder!!.playButton.setImageResource(android.R.drawable.ic_media_pause)

                    updateBottomMusicPlayer(holder.artistName.text.toString(), holder.songName.text.toString(), true)
                    resetAndStartMediaPlayer(album, position)
                } else { // trying to play song that is already playing -> pauses that song
                    startAnimation = false

                    mediaPlayer.mediaPlayer.pause()
                    adapter.colourAnimator.pause()

                    if (holder !== null)
                        holder.playButton.setImageResource(android.R.drawable.ic_media_play)

                    updateBottomMusicPlayer(artistName, songName, false)
                }
            } else {
                // playing a song that was paused
                if (mediaPlayer.dataSource == dataSet[position].path.toString()) {
                    if (holder !== null)
                        holder.playButton.setImageResource(android.R.drawable.ic_media_pause)

                    updateBottomMusicPlayer(artistName, songName, true)

                    mediaPlayer.mediaPlayer.start()

                    if (adapter.colourAnimator.isStarted)
                        adapter.colourAnimator.resume()
                    else
                        adapter.colourAnimator.start()

                    startAnimation = false
                } else { // starting a new song when no other song is playing
                    mediaPlayer.musicControlContainer.visibility = View.VISIBLE

                    resetAndStartMediaPlayer(album, position)
                    adapter.resetCurrentDividerColour(baseContext)

                    holder!!.playButton.setImageResource(android.R.drawable.ic_media_play)

                    currentSongViewHolder = holder
                    holder.playButton.setImageResource(android.R.drawable.ic_media_pause)

                    updateBottomMusicPlayer(holder.artistName.text.toString(), holder.songName.text.toString(), true)
                }
            }

            if (startAnimation) {
                adapter.colourAnimator.start()
            }
        } else {
            Log.d("bottom_player", "no adapter set")
            val isPlaying = mediaPlayer.mediaPlayer.isPlaying

            if (isPlaying) {
                mediaPlayer.mediaPlayer.pause()
            } else {
                mediaPlayer.mediaPlayer.start()
            }

            updateBottomMusicPlayer(artistName, songName, !isPlaying)
        }
    }

    fun updateBottomMusicPlayer(artistName: String, songName: String, play: Boolean) {
        mediaPlayer.musicControlButton.setImageResource(
            if (play) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
        mediaPlayer.musicTitleTextView.text = songName
        mediaPlayer.musicArtistTextView.text = artistName
    }

    fun onCompleteWithoutMusicListListener() {
        currentSongPosition =
            (currentSongPosition + 1) % (albumWiseMap[currentAlbum]?.size ?: 1)

        val mmr = MediaMetadataRetriever()
        mmr.setDataSource(albumWiseMap[currentAlbum]?.get(currentSongPosition)?.path.toString())
        val songName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE)
        val artistName = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST)
        updateBottomMusicPlayer(artistName ?: "NULL", songName ?: "NULL", true)
        resetAndStartMediaPlayer(currentAlbum!!, currentSongPosition)
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            when {
                ContextCompat.checkSelfPermission(
                    baseContext,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED -> {
                    folderSelector.launch(null)
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

    fun switchToMusicList(albumName: String) {
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.slide_out,
                R.anim.slide_in,
                R.anim.slide_out
            )
            setReorderingAllowed(true)
            replace(R.id.fragment_container, MusicListFragment.newInstance(albumName))
            addToBackStack("XYZ")
        }
    }

    fun resetAndStartMediaPlayer(album: String, position: Int) {
        val datasource = albumWiseMap[album]!![position].path.toString()
        mediaPlayer.mediaPlayer.reset()
        mediaPlayer.mediaPlayer.setDataSource(datasource)
        mediaPlayer.dataSource = datasource
        mediaPlayer.mediaPlayer.prepare()

        mediaPlayer.mediaPlayer.start()
    }

//    private fun createMusicList(pathWithoutExt: String) {
//        val path = "${Environment.getExternalStorageDirectory()}/$pathWithoutExt"
//        Log.d("custtest", path)
//
//        val parsedUri = Uri.parse(path)
//        val file = File(parsedUri.path.toString())
//
////        files = cleanFilesList(file.listFiles()!!)
//
//        if (files.isEmpty()) {
//            MaterialAlertDialogBuilder(this@MainActivity)
//                .setTitle("Error!")
//                .setMessage("The chosen directory is either empty, or does not contain any mp3/mp4 files!")
//                .setNeutralButton("OK") { dialog, _ ->
//                    dialog.dismiss()
//                }
//                .setCancelable(false)
//                .show()
//        } else {
//            val sp = getSharedPreferences(SP_FILE, MODE_PRIVATE)
//            val dirs = sp.getString("dirs", null)
//            val dirList: MutableList<String>
//            Log.d("custtest", "dirs: ${dirs.toString()}")
//
//            if (dirs !== null) {
//                dirList = dirs.split(DIR_SEPARATOR).toMutableList()
//
//                if (dirList.size == 5) {
//                    dirList.removeLast()
//                }
//
//                Log.d("custtest", dirList.toString())
//
//                if (pathWithoutExt in dirList) {
//                    dirList.removeAt(dirList.indexOf(pathWithoutExt))
//                }
//
//                dirList.add(0, pathWithoutExt)
//            } else {
//                dirList = mutableListOf(pathWithoutExt)
//            }
//
//            val spEditor = getSharedPreferences(SP_FILE, MODE_PRIVATE).edit()
//            spEditor.putString("dirs", dirList.joinToString(DIR_SEPARATOR))
//            spEditor.apply()
//
//            if (mediaPlayer.mediaPlayer.isPlaying) {
//                mediaPlayer.mediaPlayer.stop()
//                mediaPlayer.mediaPlayer.reset()
//            }
//
////            adapter = MusicListAdapter(files.toMutableList(), mediaPlayer, menuInflater, 0, musicList)
////
////            musicList.adapter = adapter
////            musicList.layoutManager = LinearLayoutManager(applicationContext)
////            val decoration: RecyclerView.ItemDecoration = DividerItemDecoration(baseContext, DividerItemDecoration.HORIZONTAL)
////            musicList.addItemDecoration(decoration)
////
////            chooseDirButton.visibility = View.GONE
////            musicList.visibility = View.VISIBLE
////            musicPlayerContainer.visibility = View.GONE
//        }
//    }

    private fun findMusicFiles(dir: File) {
        if (dir.isDirectory) {
            val files = dir.listFiles()

            if (files !== null) {
                for (i in files.indices) {
                    findMusicFiles(files[i])
                }
            }
        } else {
            if (dir.isFile && dir.name.contains(".")) {
                val extension = dir.name.substring(dir.name.lastIndexOf("."))

                if (extension in EXTENSIONS) {
                    files.add(dir)
                }
            }
        }
    }

    private fun getAlbumWiseSongs(): TreeMap<String, MutableList<File>> {
        val map = TreeMap<String, MutableList<File>>(String.CASE_INSENSITIVE_ORDER)
        val mmr = MediaMetadataRetriever()

        for (i in files) {
            mmr.setDataSource(i.path)
            var key = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ALBUM)
            key = if (key !== null) key.trim() else "Unknown Album"

            if (map.containsKey(key)) {
                map[key]!!.add(i)
            } else {
                map[key] = mutableListOf()
                map[key]?.add(i)
            }
        }

        return map
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        var openDirIcon = 0
        var closedDirIcon = 0

        when (baseContext.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK){
            Configuration.UI_MODE_NIGHT_YES -> {
                inflater.inflate(R.menu.menu_main_dark, menu)
                openDirIcon = R.drawable.ic_directory_open_white
                closedDirIcon = R.drawable.ic_directory_white
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                inflater.inflate(R.menu.menu_main_light, menu)
                openDirIcon = R.drawable.ic_directory_open_black
                closedDirIcon = R.drawable.ic_directory_black
            }
        }

        menu.getItem(0).setVisible(false)

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

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.choose_dir -> {
                requestPermission()
                true
            }

            R.id.select -> {
//                startActionMode(adapter.actionModeCallback)
                true
            }

            R.id.settings -> {
                supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    replace(R.id.fragment_container, SettingsFragment.newInstance())
                    addToBackStack("settings")
                }
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (mediaPlayer.mediaPlayer.isPlaying) {
            mediaPlayer.mediaPlayer.stop()
        }

        mediaPlayer.mediaPlayer.release()
    }

    override fun onContextItemSelected(item: MenuItem): Boolean {
//        val position = adapter.position
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}