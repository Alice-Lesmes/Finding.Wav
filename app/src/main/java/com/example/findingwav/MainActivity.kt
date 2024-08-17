package com.example.findingwav


import android.content.ContentUris
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.provider.MediaStore.Audio
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.layout.HorizontalAlignmentLine

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize

import com.example.findingwav.ui.theme.FindingWavTheme
import java.util.concurrent.TimeUnit

import org.intellij.lang.annotations.JdkConstants.HorizontalAlignment



class MainActivity : AppCompatActivity() {
    @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        // Allows to play music when using changeSong()
        var musicPlayer = MediaPlayer()
        setContent {
            FindingWavTheme {
                Scaffold(modifier =

                Modifier.fillMaxSize()) { innerPadding ->
                    
                  /*
                  //NECESSARY TO GET THE FILES. ADD THIS AS A BUTTON MAYBE OR JUST UNCOMMENT TO RUN ON LAUNCH?
                  startActivity(
                                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)

                            )
                            runBlocking {
                                // running on new thread?
                                // hopefully not blocking UI (main) thread
                                // And hopefully can be extracted out of this thread
                                var songList = getAllMusic()
                            }
                   */

                }

                // main ui
                Title("Finding Wuv", "Playlist Creation Mode", Modifier)
                Player(musicPlayer)
            }
        }
    }

    fun changeSong(songPath : Uri, mediaPlayer: MediaPlayer) {
      /**
      * This inits a music player and plays the song specified by the file path
      */
        mediaPlayer.apply {
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
        // BitMap image of the album cover. Def got to be a better file format to use but whatever
        val albumCover : Bitmap,
*/
        val artist : String,
        val duration: Int,
        )

    fun getAllMusic(): MutableList<Audio> {
        //println("Allowed to access files?: " + Environment.isExternalStorageManager())
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

            while (cursor.moveToNext()) {

                try {
                    // Assign the values of the files to these
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
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
        return dataList

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
private fun getPlayLists(): List<String> {
    return listOf("Main", "Second", "Rock") //, "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG")
}


/** Set song name, song */
fun loadSongs() {

}


@Composable
fun Title(x: String, y: String, modifier: Modifier = Modifier) {
    // the row is not row-ing
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.Red),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text(
            text = x,
            // does... something...
            fontSize = 30.sp,  // specify size
            // explanation from https://stackoverflow.com/questions/37754299/how-to-properly-set-line-height-for-android
            lineHeight = 10.sp,  // text size + padding (top and bottom) (pad = lineHeight - fontSize)
            textAlign = TextAlign.Center,
            modifier = Modifier  //.padding(top = 20.dp)  // .height makes it disappear
                .padding(top = 40.dp)  // this works
            // .background(Color.Red)
        )
        Text(
            text = y,
            //fontFamily = FontFamily.SansSerif,
            fontSize = 20.sp,
            textAlign = TextAlign.Center,
            // lineHeight = 10.sp,
            modifier = Modifier
                .padding(bottom = 10.dp)

        )
    }
}

@Composable
fun PlaylistSelect() {
    // dropdown menu for playlist select
    // Declaring a boolean value to store
    // the expanded state of the Text Field
    var mExpanded by remember { mutableStateOf(false) }

    // Create a list of cities
    val mPlaylist = getPlayLists()

    // Create a string value to store the selected city
    var mSelectedText by remember { mutableStateOf("") }

    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}

    // Up Icon when expanded and down icon when collapsed
    val icon = if (mExpanded)
        Icons.Filled.KeyboardArrowUp
    else
        Icons.Filled.KeyboardArrowDown

    Column(Modifier.padding(horizontal = 20.dp)) {

        // Create an Outlined Text Field
        // with icon and not expanded
        OutlinedTextField(
            value = mSelectedText,
            onValueChange = { mSelectedText = it },
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    // This value is used to assign to
                    // the DropDown the same width
                    mTextFieldSize = coordinates.size.toSize()
                },
            label = {Text("Playlist")},
            trailingIcon = {
                Icon(icon,"contentDescription",
                    Modifier.clickable { mExpanded = !mExpanded })
            },
            readOnly = true
        )

        // Create a drop-down menu with list of cities,
        // when clicked, set the Text Field text as the city selected
        DropdownMenu(
            expanded = mExpanded,
            onDismissRequest = { mExpanded = false },
            modifier = Modifier
                .width(with(LocalDensity.current){mTextFieldSize.width.toDp()})
        ) {
            mPlaylist.forEach { label ->
                DropdownMenuItem(onClick = {
                    mSelectedText = label
                    mExpanded = false
                },
                    text = { Text(text = label) }
                )
            }
        }
    }
}



@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Player(player: MediaPlayer) {
    var modifier = Modifier.fillMaxWidth()

    val sliderPosition = remember {
        mutableLongStateOf(0)
    }

    val currentPosition = remember {
        mutableLongStateOf(0)
    }

    val totalDuration = remember {
        mutableLongStateOf(0)
    }

    var songName by remember {
        mutableStateOf("songTitle")
    }

    var artistName by remember {
        mutableStateOf("artistTitle")
    }

    var albumImage by remember {
        mutableStateOf(R.drawable.musik)
    }

    Column (
        modifier = Modifier.padding(top = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Playlist selector
        PlaylistSelect()
        // song title (replace with song name variable
        SongTitle(songName)
        // music image
        val image = painterResource(id = albumImage)
        MusicImage(image)
        // artist name
        ArtistName(artistName)
        // accept / reject button
        AcceptReject()
        // music progress bar
        TrackSlider(
            value = sliderPosition.longValue.toFloat(),
            onValueChange = {
                sliderPosition.longValue = it.toLong()
            },
            onValueChangeFinished = {
                currentPosition.longValue = sliderPosition.longValue
                player.seekTo(sliderPosition.longValue.toInt())
            },
            songDuration = totalDuration.longValue.toFloat()
        )
        // music times
        TrackSliderTime("00:00", "03:02")
        // music controls
        Playbar()
    }
}

@Composable
fun SongTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier.padding(top = 5.dp)
    )
}

@Composable
fun MusicImage(image: Painter) {
    Box (
        modifier = Modifier
            .padding(top = 20.dp)
            .fillMaxWidth(),  // gotta remember this at all times...
        // TopCenter for horizontal, CenterStart for Vert, Center for both
        // yet it still breaks
        // https://stackoverflow.com/questions/70378231/how-to-center-vertically-children-of-box-layout-in-jetpack-compose
        contentAlignment = Alignment.TopCenter,

    ) {
        Image(
            painter = image,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            alpha = 1.0F,//opacity
            modifier = Modifier
                .background(color = Color.Gray)
                .width(200.dp)
                .height(200.dp),
        )

    }
}

@Composable
fun ArtistName(name: String) {
    Text(
        text = name,
        modifier = Modifier.padding(top = 5.dp)
    )
}

@Composable
fun AcceptReject() {
    Row(
        modifier = Modifier
            .width(200.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Reject()
        Accept()
    }

}

@Composable
fun Accept() {
    Button(onClick = { HandleAccept() },
        colors = ButtonColors(Color.Green, Color.Green, Color.Green, Color.Green),
        modifier = Modifier
            .width(70.dp)
            .height(70.dp)) {
        Image(painter = painterResource(id = R.drawable.check),
            contentDescription = null,
            contentScale = ContentScale.FillBounds)
    }
}


@Composable
fun Reject() {
    Button(
        onClick = { HandleReject() },
        colors = ButtonColors(Color.Red, Color.Red, Color.Red, Color.Red),
        modifier = Modifier
            .width(70.dp)
            .height(70.dp)
    ) {
        Image(painter = painterResource(id = R.drawable.reject), contentDescription = null)
        // make bg colour green
    }
}

// calculate nanosecond from position?

@Composable
fun TrackSliderTime(startTime: String, endTime: String) {
    Row(modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(startTime)
        Text(endTime)

    }
}

/**
 * @param
 *  value: The current position of the slider
 *  onValueChange: A lambda function that is called when the slider value changes.
 *      For example, when the user scrolling the slider. That’s why, we update our slider current value
 *      with the new one.
 *  onValueChangeFinished: A lambda function that is called when the user finishes changing the
 *      slider value. There are two cases to call this function. One is a time when the user leave the
 *      thumb of slider and the other is clicking on any point on the slider.
 *  songDuration: The total duration of the song or media being controlled by the slider.
 */
@Composable
fun TrackSlider(
    value: Float,
    onValueChange: (newValue: Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    songDuration: Float
) {
    Slider(
        value = value,
        onValueChange = {
            onValueChange(it)
        },
        onValueChangeFinished = {

            onValueChangeFinished()

        },
        valueRange = 0f..songDuration,
        colors = SliderDefaults.colors(
            thumbColor = Color.Black,
            activeTrackColor = Color.DarkGray,
            inactiveTrackColor = Color.Gray,
        ),
        modifier = Modifier
            .padding(horizontal = 50.dp)
            // .background(Color.Cyan)
    )
}


@Composable
fun Playbar() {
    Row (
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PreviousButton()
        PlayButton()
        NextButton()
    }
}

@Composable
fun PlayButton() {
    Button(
        onClick = { HandlePlay() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
    ) {
        Image(painter = painterResource(id = R.drawable.play), contentDescription = null)
    }
}

@Composable
fun PreviousButton() {
    Button(
        onClick = { PreviousSong() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
        ) {
        Image(painter = painterResource(id = R.drawable.previous), contentDescription = null)
    }
}

@Composable
fun NextButton() {
    Button(
        onClick = { HandleNextSong() },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
    ) {
        Image(painter = painterResource(id = R.drawable.next), contentDescription = null)

    }
}


/*
* START OF HANDLER METHODS
* */

/** Load the next Song */
fun NextSong() {
    println("NextSong has been called")

    // get next song title, artist name, album image and song duration.
    // update composable SongName(), ArtistName(), MusicImage() and SliderBar()
}

/** Event handler for the next song button */
fun HandleNextSong() {
    println("Handle Next Song function called")
    // get the current time stored in SliderBar.
}

/** Event Handler for the checkmark (add to playlist) */
fun HandleAccept() {
    println("Handle Accept function called")
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
fun HandlePlay() {

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

