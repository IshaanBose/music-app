package com.example.musicapp

import android.media.MediaPlayer
import android.view.View
import android.widget.ImageView
import android.widget.TextView

class CustomMediaPlayer(
    var dataSource: String,
    val mediaPlayer: MediaPlayer,
    var musicTitleTextView: TextView,
    var musicArtistTextView: TextView,
    var musicControlButton: ImageView,
    var musicControlContainer: View
){}