package com.example.findingwav

import android.content.Intent
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService

class PlaybackService : MediaSessionService() {
    private var mediaSession: MediaSession? = null

    override fun onCreate() {
        super.onCreate()
        // 1. Build the Player
        val player = ExoPlayer.Builder(this).build()

        // 2. Build the MediaSession
        mediaSession = MediaSession.Builder(this, player).build()
    }

    // This is the key method the system calls to get your session
    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    // Cleanup when the service is destroyed (e.g. app closed via swipe)
    override fun onDestroy() {
        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }
        super.onDestroy()
    }
}