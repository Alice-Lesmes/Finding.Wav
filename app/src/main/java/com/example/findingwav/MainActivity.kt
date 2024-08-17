package com.example.findingwav

import android.app.PendingIntent.getActivity
import android.content.ContentResolver
import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.content.res.AssetFileDescriptor
import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.ContactsContract.Directory
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import android.provider.MediaStore.Audio.Media
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.CodeBoy.MediaFacer.AudioGet
import com.CodeBoy.MediaFacer.BuildConfig
import com.CodeBoy.MediaFacer.MediaFacer
import com.CodeBoy.MediaFacer.mediaHolders.audioContent
import com.example.findingwav.ui.theme.FindingWavTheme
import kotlinx.coroutines.runBlocking
import java.net.URI
import java.util.concurrent.TimeUnit
import kotlin.time.Duration


class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            FindingWavTheme {

                // Inits the musicplayer. rn is loonboon
                var musicPlayerTest = MediaPlayer.create(this, R.raw.loonboon)
                Scaffold(modifier =

                Modifier.fillMaxSize()) { innerPadding ->

                    Button(onClick = {

                    }) {
                        Text(text = "Ask permission?")
                    }
                    var showTime by remember {
                        mutableStateOf(false)
                    }
                    var currentTime by remember {
                        mutableStateOf(musicPlayerTest.timestamp)
                    }
                    var songList by remember {
                        mutableStateOf(getAllMusic())
                    }

                    Column {
                        Button(modifier = Modifier.padding(20.dp), onClick = {
                            // plays music, defined on the create
                            musicPlayerTest.start()
                            showTime = true
                            currentTime = musicPlayerTest.timestamp

                            startActivity(
                                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

                            )
                            runBlocking {
                                // running on new thread?
                                // hopefully not blocking UI (main) thread
                                songList = getAllMusic()
                            }



                        }) {
                            Text(text = "play music")
                        }
                        if (showTime)
                        {
                            // Only updates on button press but whatever
                            Text("Here is the current timestamp: " + currentTime)
                        }
                    }




                }
            }
        }
    }

    fun changeSong(songPath : Uri) {
        val mediaPlayer = MediaPlayer().apply {
            setAudioAttributes(
                AudioAttributes.Builder()
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .build()
            )
            setDataSource(applicationContext, songPath)
            prepare()
            start()
        }
        println("try this idk?")
        mediaPlayer.start()

    }

    // Pulled out from the `getAllMusic()` func since it needs to be returned as well
    // And prob helpful to other code stuff
    data class Audio(
        // Path to file
        val uri: Uri,
        val name: String,
        val album : String,
/*
        val albumCover : Bitmap,
*/
        val artist : String,
        val duration: Int,
        )

    fun printAllMusic() {
            // gets all tracks
            var uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            var cr = applicationContext.contentResolver;
            println("Data: "
                    + MediaStore.Audio.Media.DISPLAY_NAME
                    + cr.query(uri, arrayOf(MediaStore.Audio.Media.DISPLAY_NAME), null, null, null))

    }

    fun getAllMusic(): MutableList<Audio> {


        // Where all the data is appended to
        val dataList = mutableListOf<Audio>()

        val collection =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL

                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI

            }
        println("DIR: " + collection)
        println("Context dir: " + applicationContext.externalMediaDirs.toString())
        val projection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ALBUM,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
        )
        val selection = "${MediaStore.Audio.Media.DURATION} >= 0 * ?"
        val selectionArgs = arrayOf(TimeUnit.MILLISECONDS.toMinutes(1).toString())
        val sortOrder = ""

        val metadataGetter : MediaMetadataRetriever = MediaMetadataRetriever()
        println("Allowed: " + Environment.isExternalStorageManager())

        val query = applicationContext.contentResolver.query(
            collection,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
            query?.use { cursor ->
                // Only assign once (i.e caching)
                val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                val nameColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                val albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                val artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                val durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)

            println("ID Column: " + artistColumn)
                println("Cursor MOve: " + cursor.moveToNext())
            while (cursor.moveToNext()) {
                println("Cursor MOve: " + cursor.moveToNext())

                try {
                    // Assign the values of the files to these
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    println(name)
                    val album = cursor.getString(albumColumn)
                    val artist = cursor.getString(artistColumn)
                    val duration = cursor.getInt(durationColumn)
                    // This is the file path of the file
                    val contentURI = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)
                    // Stupid having to convert toString() but whatever
                    metadataGetter.setDataSource(applicationContext, contentURI)
                    val rawAblum = metadataGetter.embeddedPicture
                    var length = rawAblum?.size
                    if (length == null) length = 0
                    val albumCover = BitmapFactory.decodeByteArray(rawAblum, 0, length)
                    dataList.add(MainActivity.Audio(contentURI,name,  album, artist, duration))
                }
                catch (e : Exception) {
                    println("Error! Here: " + e)
                }

            }
            dataList += Audio(collection, "Test", "Test", "Test", 30)
        }
        println(dataList)
        return dataList

    }
}



@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FindingWavTheme {
    }
}