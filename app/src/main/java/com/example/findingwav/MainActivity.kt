package com.example.findingwav


import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.findingwav.MusicPlayer
import androidx.media3.common.Player
import com.example.findingwav.ui.screens.PlayerScreen
import com.example.findingwav.ui.screens.Title
import com.example.findingwav.ui.theme.FindingWavTheme
import java.io.File
import java.util.concurrent.TimeUnit

// permission related???
import android.Manifest
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat

// persistent notification related
import androidx.media3.session.MediaController
import androidx.media3.session.SessionToken
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import android.content.ComponentName

class MainActivity : AppCompatActivity() {
    private var player: Player? = null // Use generic Player interface; NOT TO BE CONFUSED WITH PLAYER.KT.
    private var controllerFuture: ListenableFuture<MediaController>? = null
    private var musicPlayerWrapper: MusicPlayer? = null

    // Define what happens after the user clicks "Allow" or "Deny"
    private val requestMusicPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FIX: Permission just granted, load the music now!
            if (musicPlayerWrapper != null) {
                musicPlayerWrapper?.addSongs(getAllMusic())
                musicPlayerWrapper?.player?.prepare()
            }
        } else {
            Toast.makeText(this, "Music access is required to play songs", Toast.LENGTH_SHORT).show()
        }
    }

    /** To be or not to be given permission
     * Just call this to ask for consent bro
     */
    fun askForMusicPermission() {
        // 1. Determine the correct permission based on Android version
        val permissionName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO // Android 13+
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE // Android 12 and below
        }

        // request if we dont have it
        if (ContextCompat.checkSelfPermission(this, permissionName) != PackageManager.PERMISSION_GRANTED) {
            requestMusicPermissionLauncher.launch(permissionName)
        }

    }
    /** Mainly just ask for permission and enable things */
        @RequiresApi(Build.VERSION_CODES.R)
        @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //player = MusicPlayer(ExoPlayer.Builder(applicationContext).build())
        // Allows to play music when using changeSong() - OUTDATED
        // var musicPlayer = MediaPlayer()
        // Allows to play music when using changeSong(), new mediaPlayer version
        // This allows for peripherals (earphones) to properly interact with the player (not sure about skipping)
        //val mediaSession = MediaSession.Builder(applicationContext, player.player)
            // Allows to activate custom code on event
            // Currently using it to act on skip or previous
            // TODO: check if below code works for onMediaButtonAction, and for skip (double tap)
            // .setCallback()
         //   .build()
        askForMusicPermission()

        enableEdgeToEdge()


    }
    /** Start the music player */
    @RequiresApi(Build.VERSION_CODES.R)
    override fun onStart() {
        super.onStart()
        val sessionToken = SessionToken(this, ComponentName(this, PlaybackService::class.java))
        controllerFuture = MediaController.Builder(this, sessionToken).buildAsync()

        controllerFuture?.addListener({
            val controller = controllerFuture?.get()

            if (controller != null) {
                musicPlayerWrapper = MusicPlayer(controller)

                // Check if we have permission AND if we need to load music
                val hasPermission = ContextCompat.checkSelfPermission(
                    this,
                    if (Build.VERSION.SDK_INT >= 33) Manifest.permission.READ_MEDIA_AUDIO else Manifest.permission.READ_EXTERNAL_STORAGE
                ) == PackageManager.PERMISSION_GRANTED

                // Only add music if we have permission AND the player is empty (to avoid duplicates on restart)
                if (hasPermission && controller.mediaItemCount == 0) {
                    musicPlayerWrapper?.addSongs(getAllMusic())
                    musicPlayerWrapper?.player?.prepare()
                }


                setContent {
                    PlayerScreen(musicPlayerWrapper!!, applicationContext)
                }
            }
        }, MoreExecutors.directExecutor())
    }
    override fun onStop() {
        super.onStop()
        controllerFuture?.let {
            MediaController.releaseFuture(it)
        }
    }



    /**Returns the next song*/
    fun nextSong(player : ExoPlayer) : MediaItem
    {
        return player.getMediaItemAt(player.nextMediaItemIndex)
    }

    // Pulled out from the `getAllMusic()` func since it needs to be returned as well
    // And prob helpful to other code stuff
    data class Audio(
        // Path to file
        val uri: Uri,
        val name: String,
        val album : String,
        val title: String,
/*
        // BitMap image of the album cover. Def got to be a better file format to use but whatever
        val albumCover : Bitmap,
*/
        val artist : String,
        val duration: Int,
        )


    @androidx.annotation.OptIn(UnstableApi::class)
    fun getAllMusic(): MutableList<MediaItem> {
        //println("Allowed to access files?: " + Environment.isExternalStorageManager())
        // Where all the data is appended to
        val ExoDataList = mutableListOf<MediaItem>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL

                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            }
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.IS_MUSIC
        )
        // Greater than or = SelectionArgs
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        // 1 minute
        val selectionArgs = arrayOf(TimeUnit.MILLISECONDS.toMinutes(60000).toString())
        val sortOrder = ""


        val query = applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
        query?.use { cursor ->
            // Only assign once (i.e caching), the columns
            val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
            val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
            val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
            val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
            val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
            val music = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.IS_MUSIC)



            while (cursor.moveToNext()) {
                val isMusic = cursor.getString(music)
                // Check that file is music file
                if (isMusic.isNotEmpty()) {
                    // Assign the values of the files to these
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val album = cursor.getString(albumColumn)
                    val artist = cursor.getString(artistColumn)
                    val duration = cursor.getLong(durationColumn)
                    // The actual name/title of the song file
                    val title = cursor.getString(titleColumn)
                    // This is the file path of the file
                    // This is all that matters, since the player can retrieve this other data
                    val contentURI = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    val mediaItem = MediaItem.Builder().setMediaMetadata(MediaMetadata.Builder()
                        .setTitle(title)
                        .setAlbumTitle(album)
                        .setArtist(artist)
                        .setArtworkUri(contentURI)
                        .setDurationMs(duration)
                        .setDisplayTitle(name).build())
                        .setUri(contentURI).build()

                    ExoDataList.add(mediaItem)
                }
            }
        }
        ExoDataList.shuffle()
        return ExoDataList
    }

}




                    /* Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                    * Elem()
                    * } */
                        /* Surface (parameters) {
                    *   Elem()
                    * }*/


// this could be useful (making the basic music bar)
// https://www.digitalocean.com/community/tutorials/android-media-player-song-with-seekbar

fun changeSong(mediaPlayer: ExoPlayer) {
    /**
     * This inits a music player and plays the song specified by the file path
     */
    mediaPlayer.seekToNextMediaItem()
}




data class Music(
    val name: String,
    val artist: String,
    val songDuration: Float,
    val albumCover: Painter,
)
/*

// construct playlist
private fun getPlayList(): List<Music> {
    return listOf(
        Music(
            name = "loonboon",
            artist = "Laura Shigihara",
            cover = R.drawable.musik,
            music = R.raw.loonboon
        ),
    )
}
*/



/** Mock data of playlist Strings */
private fun getPlaylistNames(playlists: MutableMap<String, MutableList<MediaItem>>): List<String> {
    var names: MutableList<String> = mutableListOf();

    for (name in playlists) {
        names.add(name.key)
    }

    return names //, "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG")
}

/** Retrieve List<MainActivity.Audio> assosicated with String
 * Format is HashMap<String, List<MainActivity.Audio>>
 * */
fun retrievePlaylist(name: String) {

}


/*
* START OF HANDLER METHODS
* */

/** Load the next Song */




/** Event handler for the next song button
 * get a parameter of all the stuff? */

fun HandleNextSong() {
    println("Handle Next Song function called")
    // get the current time stored in SliderBar.

    // mock change the songTitle, artistTitle, image and duration
    // NextSong()
}

/** Event Handler for the checkmark (add to playlist) */
fun HandleAccept(currentSong: MainActivity.Audio) {
    println("Handle Accept function called")

    testM3U()
}

/** Go to previous song. To be fair, we haven't really defined logic for this yet... */
fun PreviousSong() {
    println("Previous Song has been called")
}

/** Remove the song from the loaded queue */
fun HandleReject() {
    println("Handle Reject has been called")
}

/** pause or play the song */
fun HandlePlay(currentSong: MainActivity.Audio) {
    //TODO: Play song
}


// ****************
// START OF PREVIEWS
// ****************

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FindingWavTheme {
    }
}

@Preview(showBackground = true)
@Composable
fun TitlePreview() {
    FindingWavTheme {
        Title("First Android App", "Second App")
    }
}

fun testM3U() {
    var testSong: MainActivity.Audio = MainActivity.Audio(
        Uri.parse("Music/Aja - Steely Dan (320).mp3"),
        "Music/ Steely Dan - Aja",
        "Album",
        "Aja",
        "Steely Dan",
        480
    )

    var playlist: MutableList<MainActivity.Audio> = mutableListOf<MainActivity.Audio>()
    playlist.add(testSong)

    //println(toM3U("Main", playlist))
}


/**To be used to create the .m3u file into files. Maybe works. Needs to change some params*/
// pass in playlistName
// context is applicationContext
fun createFile(playlistName: String, playlist: String, context: Context/*TODO: CHANGE THIS*/)
{
    // Request code for creating a PDF document.
    //val path = context.getExternalFilesDir(null)
    val path = Environment.getExternalStoragePublicDirectory("Music")
    File(path, "$playlistName" + ".m3u").delete()
    println("Path: " + path)
    // TODO: Add name of playlist file
    var playlistFile = File(path, "$playlistName" + ".m3u")
    // TODO: actually put playlist content, try a forEach or idk

    playlistFile.writeText("$playlist")

}


/**
 * Format is
 * #EXTM3U *Initialiser*
 * #EXTINF:RUNTIME(seconds),(noSpace)ARTIST_NAME - SONG NAME
 * FILEPATH/FILENAME
 *
 * example:
 * #EXTM3U
 * #EXTINF:480,Steely Dan - Aja
 * Music/Aja - Steely Dan (320).mp3
 *
 *
 * */
@androidx.annotation.OptIn(UnstableApi::class)
fun toM3U(playlistName: String, playlist: MutableList<MediaItem>?, context: Context) : String {
    // grab a playlist
    var out: StringBuilder = StringBuilder()

    out.append("#EXTM3U\n")
    val path = Environment.getExternalStoragePublicDirectory("Music")
    if (playlist != null) {
        for (song in playlist) {
            var metaData = song.mediaMetadata
            // Using metaData.durationMS here would necessitate deprecated/experimental stuff
            // But easier than bringing the music player all the way here
            //
            out.append("#EXTINF:").append(song.mediaMetadata.durationMs?.div(1000) ?: 1).append(",")
                .append(metaData.artist.toString())
                .append(" - ")
                // Title is the actual name of the song (maybe switch with display title)
                .append(metaData.title).append("\n")
            // Display title is the file name
            out.append(path).append("/").append(song.mediaMetadata.displayTitle).append("\n")
        }
    }
    // attempt to write locally to downloads?
//    val filePath: String = "Playlists/$playlistName"
//    val file = File(filePath)
//
//    file.writeText(out.toString())
    createFile(playlistName, out.toString(), context)


    println("Line written successfully")

    return out.toString()

}