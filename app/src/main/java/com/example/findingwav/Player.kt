package com.example.findingwav

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
import androidx.media3.common.util.UnstableApi
import com.example.findingwav.data.NECESSARY_PLAYTIME
import com.example.findingwav.data.REPEAT_SONGS

public enum class NextOpts {
    NORMAL,
    FORCEADD,
    DONTADD
}

/**
 * A class used to hold the music player, and adds extra functionality
 * Also controls Playlists, TODO: which should prob be their own class later.
 * @param player the music player to be used
 * @param songs the songs to add to the music player to play
 */
public class MusicPlayer(val player: Player, songs: List<MediaItem>? = null) {

    private var exoSongList: MutableList<MediaItem> = mutableListOf()
    private var songCount : Int = 0
    private var currentPlaylistName : String = "Main"
    private var currentPlaylist : MutableList<MediaItem> = mutableListOf()
    private var playLists : MutableMap<String, MutableList<MediaItem>> = mutableMapOf(currentPlaylistName to currentPlaylist)

    /**
     * Initialises a MusicPlayer Instance
     */
    init {
        if (songs != null) {
            addSongs(songs)
        }
        player.prepare()
        /**
         * This listener checks to see if the reason that a song was changed was
         * because the song automatically finished
         */
        player.addListener(object : androidx.media3.common.Player.Listener {
            @androidx.annotation.OptIn(UnstableApi::class)
            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // If it automatically transitioned to next song
                if (reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    println("AUTO REASON")

                    setPreviousSong(
                        player.getMediaItemAt(
                            player.previousMediaItemIndex))

                    getPreviousSong()?.let {
                        // Assuredly not Null, since if we auto progress, we have a previous song
                        addSongToCurPlaylist(getPreviousSong()!!)
                    }
                }
            }
        })
    }

    /**
     * Adds a collection of songs to the music player (to be played)
     *
     * If you were looking to add the song to the <u>playlist</u>, see addSongsToPlaylist
     * @param items the songs to be played
     * @see MediaItem
     * @see addSongsToPlaylist
     */
    fun addSongs(items : List<MediaItem>) {
        exoSongList.addAll(items)
        songCount += items.size

        // This sends the command to the Service
        player.addMediaItems(items)
    }
    /**
     * Adds a song to the music player to be played.
     *
     * If you were looking to add the song to the <u>playlist</u>, see addSongToPlaylist
     * @param item song to be added
     * @see MediaItem
     * @see addSongToPlaylist
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
    /**
     * Gets the current playlist (NOT A COPY)
     * @return the current playlist adding to
     */
    public fun getCurrentPlaylist() : MutableList<MediaItem> {
        return currentPlaylist
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
    private var previousSong : MediaItem? = null
    public fun previousSongPlayTime(playTime : Long) {
        previousSongTime = playTime
    }

    public fun getPreviousSong() : MediaItem? {
        return previousSong
    }


    public fun setPreviousSong(mediaItem: MediaItem) {
        if (mediaItem == null) {
            return
        }
        previousSong = mediaItem
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
        previousSong = getCurrentSong()
        player.seekToNextMediaItem()
        return added
    }

    public fun previousSong(add: NextOpts = NextOpts.NORMAL) : Boolean {
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
        previousSong = getCurrentSong()
        player.seekToNextMediaItem()
        return added
    }

    /**
     * Adds a song to the currently playing playlist. See REPEAT_SONGS to see
     * if multiples are allowed
     *
     * If you were looking to add a song to the list of songs to be judged, see addSong()
     *
     * It's recommended to use the previous/nextSong() functions instead of this
     * @param playlist the playlist to add the song to
     * @param song the song to add to the playlist
     * @see addSong
     * @see addSongs
     */
    public fun addSongToCurPlaylist(song: MediaItem) {
        if (currentPlaylist.contains(song) && !REPEAT_SONGS)
            return
        currentPlaylist.add(song)
    }

    /**
     * Adds songs to the currently playing playlist. See REPEAT_SONGS to see
     * if multiples are allowed
     *
     * If you were looking to add a song to the list of songs to be judged, see addSong()
     *
     * It's recommended to use the previous/nextSong() functions instead of this
     * @param songs the songs to add to the playlist
     * @see addSong
     * @see addSongs
     * @see REPEAT_SONGS
     */
    public fun addSongsToCurPlaylist(songs: List<MediaItem>) {
        songs.forEach {
            if (currentPlaylist.contains(it) && !REPEAT_SONGS) {
                return
            }
            currentPlaylist.add(it)
        }
    }

    /**
     * Adds a song to the specified playlist. See REPEAT_SONGS to see
     * if multiples are allowed
     *
     * If you were looking to add a song to the list of songs to be judged, see addSong()
     *
     * It's recommended to use the previous/nextSong() functions instead of this
     * @param playlist the playlist to add the song to
     * @param song the song to add to the playlist
     * @see addSong
     * @see addSongs
     */
    private fun addSongToPlaylist(playlist: MutableList<MediaItem>, song: MediaItem) {
        if (playlist.contains(song) && !REPEAT_SONGS)
            return
        playlist.add(song)
    }

    /**
     * Adds the songs to the specified playing playlist. See REPEAT_SONGS to see
     * if multiples are allowed
     *
     * If you were looking to add a song to the list of songs to be judged, see addSong()
     *
     * It's recommended to use the previous/nextSong() functions instead of this
     * @param playlist the playlist to add the song to
     * @param songs the songs to add to the playlist
     * @see addSong
     * @see addSongs
     * @see REPEAT_SONGS
     */
    private fun addSongsToPlaylist(playlist: MutableList<MediaItem>, songs: List<MediaItem>) {
        songs.forEach {
            if (playlist.contains(it) && !REPEAT_SONGS) {
                return
            }
            playlist.add(it)
        }
    }
}