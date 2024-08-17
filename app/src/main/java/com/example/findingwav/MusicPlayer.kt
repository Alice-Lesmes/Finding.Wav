package com.example.findingwav


import android.media.AudioAttributes
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MusicPlayerTest : AppCompatActivity() {


    fun playMusic() {

        var music = MediaPlayer.create(this, R.raw.test_song)
        music.start();



    }
}