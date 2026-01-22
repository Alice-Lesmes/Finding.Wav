package com.example.findingwav

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.app.DownloadManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import java.io.File
import java.io.OutputStream

/** Writes to downloads using mediastore api */
@RequiresApi(Build.VERSION_CODES.Q)
fun savePlaylistToDownloads(context: Context, playlistName: String, content: String) {
    try {
        val resolver = context.contentResolver
        val fileName = "$playlistName.m3u"

        // 1. Setup the file details
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "audio/x-mpegurl")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        // 2. Ask MediaStore to create the file entry
        // This works even on Android 11+ without special permissions
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

        if (uri != null) {
            // 3. Open the stream and write the data
            val outputStream: OutputStream? = resolver.openOutputStream(uri)
            outputStream?.use { stream ->
                stream.write(content.toByteArray())
            }

            // 4. Success! Show Toast
            Toast.makeText(context, "Saved $fileName to Downloads", Toast.LENGTH_SHORT).show()

            // 5. Open Downloads App
            openDownloadsFolder(context)
        } else {
            Toast.makeText(context, "Failed to create file", Toast.LENGTH_SHORT).show()
        }

    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_LONG).show()
    }
}

// Helper to open the folder
fun openDownloadsFolder(context: Context) {
    try {
        val intent = Intent(DownloadManager.ACTION_VIEW_DOWNLOADS).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        context.startActivity(intent)
    } catch (e: Exception) {
        // Fallback for some devices that don't support this intent
        println("Could not open downloads: ${e.message}")
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
@RequiresApi(Build.VERSION_CODES.Q)
@androidx.annotation.OptIn(UnstableApi::class)
fun toM3U(playlistName: String, playlist: MutableList<MediaItem>?, context: Context) : String {
    // grab a playlist
    var out: StringBuilder = StringBuilder()

    out.append("#EXTM3U\n")
    //val path = Environment.getExternalStoragePublicDirectory("Music"
    val path = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
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
//
//    file.writeText(out.toString())
    savePlaylistToDownloads(context, playlistName, out.toString())

    Toast.makeText(context, "Playlist saved successfully!", Toast.LENGTH_SHORT).show()
    openDownloadsFolder(context)

    return out.toString() // to be fair I dont think we need to actually return this

}

