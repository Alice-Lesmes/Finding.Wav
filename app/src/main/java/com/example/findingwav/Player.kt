package com.example.findingwav

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.exoplayer.ExoPlayer
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
public class MusicPlayer {

    public lateinit var player : ExoPlayer
    private lateinit var exoSongList : MutableList<MediaItem>
    private var songCount : Int = 0

    private var currentPlaylistName : String = "Main"

    /**
     * The playlist we are currently curating/creating
     */
    private var currentPlaylist : MutableList<MediaItem> = mutableListOf()

    private var playLists : MutableMap<String, MutableList<MediaItem>> = mutableMapOf<String, MutableList<MediaItem>>(currentPlaylistName to currentPlaylist)

    /**
     * Initialises a MusicPlayer Instance
     */
    fun onCreate(context : Context) {
       player = ExoPlayer.Builder(context).build()
    }


    public fun getSongList() : MutableList<MediaItem>
    {
        return exoSongList
    }

    public fun getCurrentPlaylist() : MutableList<MediaItem> {
        var list : MutableList<MediaItem> = mutableListOf()
        list.addAll(currentPlaylist)
        return list
    }

    public fun getCurrentPlaylistName() : String {
        return currentPlaylistName
    }
    public fun getPlaylist(name : String) : MutableList<MediaItem>? {
        return playLists.get(name)
    }

    public fun getPreviousSong(player : ExoPlayer) : MediaItem
    {
        return player.getMediaItemAt(player.previousMediaItemIndex)
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
        playLists.put(name, mutableListOf())
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
        playLists.put(name, items)
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
    public fun getCurrentSong() : MediaItem?
    {
        val song = player.currentMediaItem
        return song
    }

    /**
     * Determines the current song/MediaItem
     * @see 'To <b>query</b> this item for it's metadata use getCurrentSong().mediaMetadata.xxxx'
     * @return the current MediaItem (song) being played, else if retNull is <b>true</b> null,
     * else retNull is <b>false</b> a special media item is returned with a custom image and title
     * <b>EXAMPLE:</b> retNull = false && no Current Song, returns: Title = "End of List"; AlbumTitle = "No
     * More Songs"
     */
    public fun getCurrentSong(retNull : Boolean) : MediaItem?
    {
        val song = player.currentMediaItem
        if (!retNull && song == null) {
            println("Song is null")
            // No data. End of list item or list is empty
            return MediaItem.Builder().setMediaMetadata(MediaMetadata.Builder()
                .setTitle("End of List")
                .setAlbumTitle("No More Songs")
                .build()).build()

        }
        return song
    }

    // Used to check the previous song's play time (to see whether to add to playlist)
    companion object {
        var previousSongTime : Long = 0
        public fun previousSongPlayTime(playTime : Long) {
            previousSongTime = playTime
        }

    }

    /**
     * Moves to the next song while also executing adding logic to the current playlist
     * @param add whether to add when reaching NECESSARY_PLAYTIME of the song (NORMAL);
     * Forcefully add (FORCEADD); or Not add (DONTADD)
     * @return true iff song has been added, else false
     */
    public fun nextSong(add: NextOpts = NextOpts.NORMAL) : Boolean {
        previousSongPlayTime(player.currentPosition)
        var added : Boolean = false
        if (add == NextOpts.FORCEADD ||
            (add == NextOpts.NORMAL &&
                    (player.currentPosition >= NECESSARY_PLAYTIME * player.duration))) {
            if (player.currentMediaItem != null) {
                addSongToPlaylist(currentPlaylist, player.currentMediaItem!!)
                added = true
            }
        }
        player.seekToNextMediaItem()
        return added
    }

    /** Function for use in NextButton and Accept().
     * Requires both playlist and currently playing song to be passed.
     * */
    private fun addSongToPlaylist(playlist: MutableList<MediaItem>, song: MediaItem) {
        playlist.add(song)
        // print playlist
        println(playlist.toString())
    }
}