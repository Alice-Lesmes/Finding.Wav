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

/**
 * A class used to hold the ExoPlayer player, and adds extra functionality.
 * Also controls Playlists, TODO: which should prob be their own class later.
 */
public class MusicPlayer(var player: Player) {

    private var exoSongList: MutableList<MediaItem> = mutableListOf()

    private var songCount : Int = 0
    private var currentPlaylistName : String = "Main"
    private var currentPlaylist : MutableList<MediaItem> = mutableListOf()
    private var playLists : MutableMap<String, MutableList<MediaItem>> = mutableMapOf(currentPlaylistName to currentPlaylist)

    /**
     * Initialises a MusicPlayer Instance
     * @see addSongs
     * @see addSong
     */
    fun onCreate() {
        player.prepare()
    }
    /**
     * Initialises a MusicPlayer Instance with some songs pre-loaded
     */
    fun onCreate(context : Context, items : MutableList<MediaItem>) {
        // reuse the logic below to keep lists in sync
        addSongs(items)
        player.prepare()

    }

    /**
     * Adds a collection of songs to the music player (to be played)
     * @param items the songs to be played
     * @see MediaItem
     */
    fun addSongs(items : MutableList<MediaItem>) {
        exoSongList.addAll(items)
        songCount += items.size

        // This sends the command to the Service
        player.addMediaItems(items)
    }
    /**
     * Adds a song to the music player to be played
     * @param item song to be added
     * @see MediaItem
     */
    fun addSong(item : MediaItem) {
        // Sync local list
        exoSongList.add(item)

        // Send to service
        player.addMediaItem(item)
    }
    /**
     * Returns a copy of all the media items in the song player
     * @return copy of mutable list of MediaItems
     * @see MediaItem
     */
    public fun getSongList() : MutableList<MediaItem> {
        // Now this is safe because exoSongList is initialized
        return ArrayList(exoSongList)
    }

    /**
     * Sets the current playlist to an already stored playlist, using name
     * @param name name of pre-existing playlist
     * @return true iff `name` exists in pre-existing playlist list and has swapped to player to
     * that playlist, else false
     */
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


    /**
     * Sets the current playlist to an already stored playlist, using name
     * @param name name of pre-existing playlist
     * @return true iff `name` exists in pre-existing playlist list and has swapped to player to
     * that playlist, else false
     */
    public fun setCurrentPlaylist(name: String) : Boolean {
        if (getPlaylist(name) != null) {
            currentPlaylist = getPlaylist(name)!!
            return true
        }
        return false
    }

    /**
     * Adds a playlist to the list of playlists. The playlist added is empty, if not desired, use
     * overloaded fun.
     * @param name the name of the new/replacing playlist
     * @param replace whether to replace an old playlist with the same name
     * @return true iff has successfully (re)placed playlist into list of playlists, else false
     */
    public fun addPlaylist(name: String, replace : Boolean) : Boolean {
        if (!replace && getPlaylist(name) != null) {
            return false
        }
        playLists[name] = mutableListOf()
        return true
    }
    /**
     * Adds a playlist to the list of playlists.
     * @param name the name of the new/replacing playlist
     * @param replace whether to replace an old playlist with the same name
     * @param items media items (songs) in the playlist
     * @return true iff has successfully (re)placed playlist into list of playlists, else false
     */
    public fun addPlaylist(name: String, replace : Boolean, items : MutableList<MediaItem>) : Boolean {
        if (!replace && getPlaylist(name) != null) {
            return false
        }
        playLists[name] = items
        return true
    }
    /**
     * Removes a specified playlist
     * @param name the name of the playlist to remove
     * @return true iff playlist exists and has been removed, else false
     */
    public fun removePlaylist(name: String) : Boolean {
        return (playLists.remove(name) != null)
    }
    /**
     * Determines the current song/MediaItem
     * @see 'To <b>query</b> this item for it's metadata use getCurrentSong().mediaMetadata.xxxx'
     * @return the current MediaItem (song) being played, else null
     */
    public fun getCurrentSong() : MediaItem? {
        return player.currentMediaItem
    }
    /**
     * Determines the current song/MediaItem
     * @see 'To <b>query</b> this item for it's metadata use getCurrentSong().mediaMetadata.xxxx'
     * @return the current MediaItem (song) being played, else if retNull is <b>true</b> null,
     * else retNull is <b>false</b> a special media item is returned with a custom image and title
     * <b>EXAMPLE:</b> retNull = false && no Current Song, returns: Title = "End of List"; AlbumTitle = "No
     * More Songs"
     */
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

    /**
     * Moves to the next song while also executing adding logic to the current playlist
     * @param add whether to add when reaching NECESSARY_PLAYTIME of the song (NORMAL);
     * Forcefully add (FORCEADD); or Not add (DONTADD)
     * @return true iff song has been added, else false
     */
    public fun getPreviousSong() : MediaItem? {
        if (player.previousMediaItemIndex != -1) {
            return player.getMediaItemAt(player.previousMediaItemIndex)
        }
        return null
    }

    // TODO: Store previous song, instead of relying on player.previousMediaItemIndex

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