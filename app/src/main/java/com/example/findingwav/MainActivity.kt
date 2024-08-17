package com.example.findingwav


import android.content.ContentUris
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.AudioAttributes
import android.media.MediaMetadataRetriever
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.DocumentsContract
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.collection.ObjectList
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
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shadow
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale

import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.app.ActivityCompat.startActivity
import androidx.core.app.ActivityCompat.startActivityForResult
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.example.findingwav.MainActivity.Audio

import com.example.findingwav.ui.theme.FindingWavTheme
import java.io.File

import java.io.FileOutputStream

import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity() {

    private lateinit var songList : MutableList<Audio>
    private var songCount : Int = 0

    private var currentPlaylistName : String = "Main"
    private var currentPlaylist : MutableList<Audio> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.R)
    fun setSongList() {
        // If have permissions just do it
        if (Environment.isExternalStorageManager())
        {
            songList = getAllMusic()
        }
        else
        {
            startActivity(
                Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
            )
            songList = getAllMusic()
        }
    }

    private var playLists : MutableMap<String, MutableList<Audio>> = mutableMapOf<String, MutableList<Audio>>(currentPlaylistName to currentPlaylist)

    public fun getSongList() : MutableList<Audio>
    {
        return songList
    }
    public fun getCurrentSong() : Audio
    {
        return songList[songCount]
    }
    public fun getPlaylist(name : String) : MutableList<Audio>? {
        return playLists.get(name)
    }


        @RequiresApi(Build.VERSION_CODES.R)
        @OptIn(ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setSongList()
        enableEdgeToEdge()
        // Allows to play music when using changeSong()
        var musicPlayer = MediaPlayer()



        setContent {
            FindingWavTheme {
                Scaffold(modifier =

                Modifier.fillMaxSize()) { innerPadding ->
                }

                // main ui
                Title("Finding Wuv", "Playlist Creation Mode", Modifier)
                Export(currentPlaylistName, getPlaylist(currentPlaylistName), applicationContext)
                var currentSong by remember {
                    mutableStateOf(getCurrentSong())
                }


                musicPlayer.setOnCompletionListener {
                    println("finished song: " + currentSong.name)
                    addSongToPlaylist(currentPlaylist, currentSong)
                    currentSong = nextSong()
                    changeSong(currentSong.uri, musicPlayer, applicationContext)
                }

                Player(musicPlayer,
                    currentSong,
                    applicationContext,
                    makeImage(currentSong.uri),
                    onAccept = {
                        currentSong = nextSong()
                        if (musicPlayer.isPlaying) changeSong(currentSong.uri, musicPlayer, applicationContext)

                        addSongToPlaylist(currentPlaylist, currentSong)
                    },
                    onReject = {
                        nextSong()
                        if (musicPlayer.isPlaying) changeSong(currentSong.uri, musicPlayer, applicationContext)

                    },
                    skipSong = {
                        // If time played is greater than or equal to 90% of the duration add to playlist
                        if (musicPlayer.currentPosition >= 0.9 * musicPlayer.duration)
                        {
                            println("Reached threshold, adding to playlist")
                            addSongToPlaylist(currentPlaylist, currentSong)
                        }
                        currentSong = nextSong()
                        if (musicPlayer.isPlaying) changeSong(currentSong.uri, musicPlayer, applicationContext)

                    }
                )

                

            }
        }
    }

    /**Returns the next song*/
    fun nextSong() : Audio
    {
        songCount++
        return getCurrentSong()
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
        // Greater than or = SelectionArgs
        val selection = "${MediaStore.Audio.Media.DURATION} >= ?"
        // 1 minute
        val selectionArgs = arrayOf(TimeUnit.MILLISECONDS.toMinutes(1).toString())
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

                    dataList.add(MainActivity.Audio(contentURI,name,  album, artist, duration))
                }
                catch (e : Exception) {
                    println("Error! Here: " + e)
                }

            }
        }
        return dataList

    }

    public fun makeImage(filePath : Uri) : Bitmap {
        /**
         * Returns a Bitmap image of the album cover (if none, bitmap is like 0x0 image)
         */
        val metadataGetter : MediaMetadataRetriever = MediaMetadataRetriever()

        metadataGetter.setDataSource(applicationContext, filePath)
        val rawAlbum = metadataGetter.embeddedPicture
        var length = 0
        try {
            println(getCurrentSong().name)
            length = rawAlbum!!.size
        }
        catch (e: Exception){
            println("Ooops no Album Image")
            return R.drawable.musik.toDrawable().toBitmap(width = 256, height = 256)
        }
        if (length == 0 || rawAlbum == null)
        {
            return R.drawable.musik.toDrawable().toBitmap(width = 256, height = 256)
        }
        var bitmap = BitmapFactory.decodeByteArray(rawAlbum, 0, length)
        if (bitmap == null) return R.drawable.musik.toDrawable().toBitmap(width = 256, height = 256)
        return bitmap

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

fun changeSong(songPath : Uri, mediaPlayer: MediaPlayer, context: Context) {
    /**
     * This inits a music player and plays the song specified by the file path
     */
    mediaPlayer.reset()
    mediaPlayer.apply {
        setAudioAttributes(
            AudioAttributes.Builder()
                .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .build()
        )
        setDataSource(context, songPath)
        prepare()
        start()
    }
    mediaPlayer.start()

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
private fun getPlayLists(): List<String> {

    return listOf("Main", "Second", "Rock") //, "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG", "SUPER LONG")
}

/** Retrieve List<MainActivity.Audio> assosicated with String
 * Format is HashMap<String, List<MainActivity.Audio>>
 * */
fun retrievePlaylist(name: String) {

}

// this can probably be deleted
open class GetParameters {
    open var songName: String = "songTitle"

    open fun getName(): String {
        return songName
    }

    open fun setName(name: String) {
        songName = name
    }
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


/** Export the current playlist */
@Composable
fun Export(playlistName: String, playlist: MutableList<MainActivity.Audio>?, context: Context) {
    Button(
        onClick = { toM3U(playlistName, playlist, context) },
        modifier = Modifier
            .padding(start = 10.dp, top = 20.dp)
    ) {
        Image(
            painter = painterResource(id = R.drawable.export),
            contentDescription = null,
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
fun Player(
    player: MediaPlayer,
    currentSong: MainActivity.Audio,
    context: Context,
    image: Bitmap,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    skipSong: () -> Unit) {
    var modifier = Modifier.fillMaxWidth()
    // Get currentSong as Audio class
   /* var songCount by remember {
        mutableIntStateOf(initSongCount)
    }
    var currentSong by remember {
        mutableStateOf(songList[songCount])
    }*/
    val sliderPosition = remember {
        mutableLongStateOf(currentSong.duration.toLong())
    }

    val currentPosition = remember {
        mutableLongStateOf(0)
    }

    /*val totalDuration = remember {
        mutableLongStateOf(currentSong.duration.toLong())
    }*/




    var artistName by remember {
        mutableStateOf(currentSong.artist)
    }

    var albumImage by remember {
        mutableStateOf(image)
    }

    Column (
        modifier = Modifier.padding(top = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Playlist selector
        PlaylistSelect()
        // song title (replace with song name variable
        SongTitle(currentSong.name)
        // music image
        MusicImage(image)
        // artist name

        ArtistName(currentSong.artist)
        // accept / reject button
        AcceptReject(
            onAccept = {
                onAccept()
                println(currentSong)


            },
            onReject = {
                onReject()

                if (player.isPlaying) changeSong(currentSong.uri, player, context)

            }
        )
        var totalDuration = currentSong.duration.toLong()
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
            songDuration = totalDuration.toFloat()
        )
        // music times
        var minutes = totalDuration / (60 * 1000)
        var seconds = (totalDuration / 1000) % 60
        var minutesString = minutes.toString()
        var secondsString = seconds.toString()
        if (minutes < 10) {
            minutesString = "0" + minutesString
        }
        if (seconds < 10) {
            secondsString = "0" + secondsString
        }
        TrackSliderTime("00:00", "$minutesString:$secondsString")
        // music controls
        Playbar(currentSong, player, context, skipSong = { skipSong() })
    }
}

@Composable
fun SongTitle(title: String) {
    Text(
        text = title,
        modifier = Modifier
            .padding(top = 5.dp),
        color = Color.White
    )
}



@Composable
fun MusicImage(image: Bitmap) {
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
            bitmap = image.asImageBitmap(),
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
        modifier = Modifier.padding(top = 5.dp),
        color = Color.White
    )
}

@Composable
fun AcceptReject(onAccept : () -> Unit, onReject: () -> Unit) {
    Row(
        modifier = Modifier
            .width(200.dp)
            .padding(top = 10.dp),
        horizontalArrangement = Arrangement.SpaceBetween

    ) {
        Reject(onReject)
        Accept(onAccept)
    }

}

@Composable
fun Accept(onAccept: () -> Unit) {
    Button(onClick =  {
        onAccept()
    },
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
fun Reject(onReject: () -> Unit) {
    Button(
        onClick = {

            onReject()},
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
        Text(startTime, style = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 1.0f)))
        Text(endTime, style = TextStyle(shadow = Shadow(color = Color.White, blurRadius = 1.0f)))


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
fun Playbar(currentSong: MainActivity.Audio, mediaPlayer: MediaPlayer, context: Context, skipSong: () -> Unit) {
    Row (
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PreviousButton()
        PlayButton(currentSong.uri, mediaPlayer, context)
        NextButton(skipSong = {
            skipSong()

        })
    }
}

@Composable
fun PlayButton(songPath : Uri, mediaPlayer: MediaPlayer, context: Context, ) {
    var playing by remember {
        mutableStateOf(mediaPlayer.isPlaying)
    }
    if (!playing)
    {
        Button(
            onClick = { changeSong(songPath, mediaPlayer, context); playing = true  },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
        ) {
            Image(painter = painterResource(id = R.drawable.play), contentDescription = null)
        }
    }
    else
    {
        Button(
            onClick = { mediaPlayer.pause(); playing = false  },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
        ) {
            Image(painter = painterResource(id = R.drawable.pause), contentDescription = null, contentScale = ContentScale.FillBounds )
        }
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
fun NextButton(skipSong : () -> Unit) {
    Button(
        onClick = { skipSong()},
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
    // update composable SongTitle(), ArtistName(), MusicImage() and SliderBar()
    //SongTitle("New Song")
    //ArtistName(name = "New Artist")
    //TrackSliderTime("00:00", "06:00")

}


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
        "Aja",
        "Album",
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
    val path = context.getExternalFilesDir(null)
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
fun toM3U(playlistName: String, playlist: MutableList<MainActivity.Audio>?, context: Context) : String {
    // grab a playlist
    var out: StringBuilder = StringBuilder()

    out.append("#EXTM3U\n")

    if (playlist != null) {
        for (song in playlist) {
            out.append("#EXTINF:").append(song.duration).append(",").append(song.artist).append(" - ").append(song.name).append("\n")
            out.append(song.uri)
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

/** Function for use in NextButton and Accept().
 * Requires both playlist and currently playing song to be passed.
 * */
fun addSongToPlaylist(playlist: MutableList<MainActivity.Audio>, song: MainActivity.Audio) {
    playlist.add(song)
    // print playlist
    println(playlist.toString())
}
