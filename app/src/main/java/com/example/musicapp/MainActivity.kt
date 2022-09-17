package com.example.musicapp

import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import android.view.*
import android.widget.PopupMenu
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
    lateinit var spFile: String
    lateinit var username: String
    var currentAlbum: String? = null
    var currentSongPosition: Int = 0
    var adapter: MusicListAdapter? = null
    var currentSongViewHolder: MusicListAdapter.ViewHolder? = null
    var isStopped = false

    val DIR_SEPARATOR = "<>"
    private val POPUP_MENU_FIRST = 100
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
            showAlbumListFromPath(pathWithoutExt)
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
                setCustomAnimations(
                    R.anim.slide_in,
                    R.anim.fade_out,
                    R.anim.slide_in,
                    R.anim.fade_out
                )
                setReorderingAllowed(true)
                add(R.id.fragment_container, ButtonFragment.newInstance())
            }
        }

        username = intent.extras!!.getString("username").toString()
        spFile = "com.musicapp.$username"

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

                    if (isStopped)
                        mediaPlayer.mediaPlayer.prepare()

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
//                    Log.d("view_holder", "Setting view holder in starting new song when no other song is playing: ${holder!!.songName.text}")
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

        isStopped = false
    }

    fun updateBottomMusicPlayer(artistName: String, songName: String, play: Boolean) {
        mediaPlayer.musicControlButton.setImageResource(
            if (play) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play
        )
        mediaPlayer.musicTitleTextView.text = songName
        mediaPlayer.musicArtistTextView.text = artistName
    }

    fun resetAndStartMediaPlayer(album: String, position: Int) {
        val datasource = albumWiseMap[album]!![position].path.toString()
        mediaPlayer.mediaPlayer.reset()
        mediaPlayer.mediaPlayer.setDataSource(datasource)
        mediaPlayer.dataSource = datasource
        mediaPlayer.mediaPlayer.prepare()

        mediaPlayer.mediaPlayer.start()
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
                R.anim.fade_out,
                R.anim.slide_in,
                R.anim.fade_out
            )
            setReorderingAllowed(true)
            replace(R.id.fragment_container, MusicListFragment.newInstance(albumName))
            addToBackStack("XYZ")
        }
    }

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

    private fun showAlbumListFromPath(path: String) {
        val sp = getSharedPreferences("$spFile.dirs", MODE_PRIVATE)
        val dirs = sp.getString("dirs", null)
        val dirList: MutableList<String>

        if (dirs !== null) {
            dirList = dirs.split(DIR_SEPARATOR).toMutableList()

            if (dirList.size == getSharedPreferences("$spFile.settings", MODE_PRIVATE).getInt(SettingsFragment.SP_DEF_SEEK_KEY, 5)) {
                dirList.removeLast()
            }

            if (path in dirList) {
                dirList.removeAt(dirList.indexOf(path))
            }

            dirList.add(0, path)
        } else {
            dirList = mutableListOf(path)
        }

        val spEditor = sp.edit()
        spEditor.putString("dirs", dirList.joinToString(DIR_SEPARATOR))
        spEditor.apply()

        val filePath = "${Environment.getExternalStorageDirectory()}/$path"
        val parsedUri = Uri.parse(filePath)
        val file = File(parsedUri.path.toString())
        files = mutableListOf()

        findMusicFiles(file)
        albumWiseMap = getAlbumWiseSongs()
        supportFragmentManager.commit {
            setCustomAnimations(
                R.anim.slide_in,
                R.anim.fade_out,
                R.anim.slide_in,
                R.anim.fade_out
            )
            setReorderingAllowed(true)
            replace(R.id.fragment_container, AlbumViewFragment.newInstance())
        }
    }

    fun logout() {
        startActivity(Intent(baseContext, StartingActivity::class.java))
        finish()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        val inflater: MenuInflater = menuInflater
        val openDirIcon = R.drawable.ic_directory_open_white
        val closedDirIcon = R.drawable.ic_directory_white
        inflater.inflate(R.menu.menu_main_dark, menu)

        menu.getItem(0).setVisible(false)

        Handler(Looper.myLooper()!!).post(Runnable {
            Log.d("menu_click", "hallo")
            val view = findViewById<View>(R.id.menu_choose_dir)

            if (view !== null) {
                view.setOnLongClickListener(View.OnLongClickListener {
                    val popupMenu = PopupMenu(baseContext, it)
                    val sp = getSharedPreferences("$spFile.dirs", MODE_PRIVATE)
                    val dirs = sp.getString("dirs", null)

                    if (dirs !== null) {
                        menu.getItem(1).setIcon(openDirIcon)
                        val dirList = dirs.split(DIR_SEPARATOR)

                        for (i in dirList.indices) {
                            popupMenu.menu.add(0, POPUP_MENU_FIRST + i, i, dirList[i])
                                .setIcon(R.drawable.ic_directory_open_black)
                                .setOnMenuItemClickListener {
                                    showAlbumListFromPath(dirList[i])
                                    true
                                }
                                .setShowAsAction(MenuItem.SHOW_AS_ACTION_WITH_TEXT)

                            popupMenu.setOnDismissListener {
                                menu.getItem(1).setIcon(closedDirIcon)
                            }
                        }

                        popupMenu.show()
                    } else {
                        Toast.makeText(baseContext, "No recent directories!", Toast.LENGTH_LONG).show()
                    }

                    true
                })
            } else {
                Log.d("menu_click", "Menu item null?????")
            }
        })

        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_choose_dir -> {
                requestPermission()
                true
            }

            R.id.menu_settings -> {
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

    override fun onBackPressed() {
        if (supportFragmentManager.backStackEntryCount != 0) {
            supportFragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}