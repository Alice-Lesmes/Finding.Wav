package com.example.findingwav

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import com.example.findingwav.data.NECESSARY_PLAYTIME

public enum class NextOpts {
    NORMAL,
    FORCEADD,
    DONTADD
}

// Constructor is already correct (accepts Player), so it works with MediaController
public class MusicPlayer(val player: Player) {

    private var exoSongList: MutableList<MediaItem> = mutableListOf()

    private var songCount : Int = 0
    private var currentPlaylistName : String = "Main"
    private var currentPlaylist : MutableList<MediaItem> = mutableListOf()
    private var playLists : MutableMap<String, MutableList<MediaItem>> = mutableMapOf(currentPlaylistName to currentPlaylist)


    fun onCreate() {
    }

    fun onCreate(context : Context, items : MutableList<MediaItem>) {
        // reuse the logic below to keep lists in sync
        addSongs(items)
    }

    /**
     * Adds a collection of songs to the music player
     */
    fun addSongs(items : MutableList<MediaItem>) {
        exoSongList.addAll(items)
        songCount += items.size

        // This sends the command to the Service
        player.addMediaItems(items)
    }

    fun addSong(item : MediaItem) {
        // Sync local list
        exoSongList.add(item)

        // Send to service
        player.addMediaItem(item)
    }

    public fun getSongList() : MutableList<MediaItem> {
        // Now this is safe because exoSongList is initialized
        return ArrayList(exoSongList)
    }

    public fun getCurrentPlaylist() : MutableList<MediaItem> {
        return ArrayList(currentPlaylist)
    }

    public fun getCurrentPlaylistName() : String {
        return currentPlaylistName
    }

    public fun getPlaylist(name : String) : MutableList<MediaItem>? {
        return playLists[name]
    }

    public fun getPlaylists() : MutableMap<String, MutableList<MediaItem>> {
        return playLists
    }

    // <--- CHANGE 4: Removed argument 'player: ExoPlayer'.
    // We use the class property 'this.player' which is the generic interface.
    public fun getPreviousSong() : MediaItem? {
        if (player.previousMediaItemIndex != -1) {
            return player.getMediaItemAt(player.previousMediaItemIndex)
        }
        return null
    }

    public fun setCurrentPlaylist(name: String) : Boolean {
        if (getPlaylist(name) != null) {
            currentPlaylist = getPlaylist(name)!!
            return true
        }
        return false
    }

    public fun addPlaylist(name: String, replace : Boolean) : Boolean {
        if (!replace && getPlaylist(name) != null) {
            return false
        }
        playLists[name] = mutableListOf()
        return true
    }

    public fun addPlaylist(name: String, replace : Boolean, items : MutableList<MediaItem>) : Boolean {
        if (!replace && getPlaylist(name) != null) {
            return false
        }
        playLists[name] = items
        return true
    }

    public fun removePlaylist(name: String) : Boolean {
        return (playLists.remove(name) != null)
    }

    public fun getCurrentSong() : MediaItem? {
        return player.currentMediaItem
    }

    public fun getCurrentSong(retNull : Boolean) : MediaItem? {
        val song = player.currentMediaItem
        if (!retNull && song == null) {
            return MediaItem.Builder().setMediaMetadata(MediaMetadata.Builder()
                .setTitle("End of List")
                .setAlbumTitle("No More Songs")
                .build()).build()
        }
        return song
    }

    var previousSongTime : Long = 0
    public fun previousSongPlayTime(playTime : Long) {
        previousSongTime = playTime
    }

    public fun nextSong(add: NextOpts = NextOpts.NORMAL) : Boolean {
        previousSongPlayTime(player.currentPosition)
        var added : Boolean = false

        // Added check for duration > 0 to prevent issues when song is loading
        val duration = player.duration
        if (duration > 0 && (add == NextOpts.FORCEADD ||
                    (add == NextOpts.NORMAL &&
                            (player.currentPosition >= NECESSARY_PLAYTIME * duration)))) {

            if (player.currentMediaItem != null) {
                addSongToPlaylist(currentPlaylist, player.currentMediaItem!!)
                added = true
            }
        }
        player.seekToNextMediaItem()
        return added
    }

    private fun addSongToPlaylist(playlist: MutableList<MediaItem>, song: MediaItem) {
        playlist.add(song)
        println(playlist.toString())
    }
}