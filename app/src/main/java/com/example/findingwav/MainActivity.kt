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
import android.provider.MediaStore
import android.provider.Settings
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.core.graphics.drawable.toBitmap
import androidx.core.graphics.drawable.toDrawable
import com.example.findingwav.MainActivity.Audio
import com.example.findingwav.ui.theme.FindingWavTheme
import com.github.theapache64.twyper.SwipedOutDirection
import com.github.theapache64.twyper.Twyper
import com.github.theapache64.twyper.TwyperController
import com.github.theapache64.twyper.rememberTwyperController
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
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
        else {
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


        try {
            return songList[songCount]

        }
        catch (e : Exception) {
            println("Reached end of list")
            return Audio(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                name ="End of List",
                album = "No More Songs",
                title = "Reached end of all songs. Export Playlist",
                artist = "Export",
                duration = 10,
            )
        }
    }
    public fun getPreviousSong() : Audio
    {
        songCount--
        if (songCount < 0)
        {
            songCount = 0
        }
        return getCurrentSong()
    }
    public fun getPlaylist(name : String) : MutableList<Audio>? {
        return playLists.get(name)
    }
    public fun setCurrentPlaylist(name: String) {
        if (getPlaylist(name) != null) {
            currentPlaylist = getPlaylist(name)!!
        }
    }
    public fun addPlaylist(name: String) {
        playLists.put(name, mutableListOf())
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
                Edit(getPlaylist(currentPlaylistName))
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
                        addSongToPlaylist(currentPlaylist, currentSong)
                        if (musicPlayer.isPlaying) {
                            currentSong = nextSong()
                            changeSong(currentSong.uri, musicPlayer, applicationContext)
                        }
                        else
                        {
                            currentSong = nextSong()
                        }
                    },
                    onReject = {
                        if (musicPlayer.isPlaying) {
                            currentSong = nextSong()
                            changeSong(currentSong.uri, musicPlayer, applicationContext)
                        }
                        else
                        {
                            currentSong = nextSong()
                        }

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

                    },
                    playLists,
                    selectPlaylist = {setCurrentPlaylist(currentPlaylistName)},
                    currentPlaylistName,
                    previousSong = { currentSong = getPreviousSong() }
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
        val title: String,
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
            MediaStore.Audio.Media.TITLE
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
            val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)

            while (cursor.moveToNext()) {

                try {
                    // Assign the values of the files to these
                    val id = cursor.getLong(idColumn)
                    val name = cursor.getString(nameColumn)
                    val album = cursor.getString(albumColumn)
                    val artist = cursor.getString(artistColumn)
                    val duration = cursor.getInt(durationColumn)
                    // The actual name/title of the song file
                    val title = cursor.getString(titleColumn)
                    // This is the file path of the file
                    val contentURI = ContentUris.withAppendedId(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id)

                    dataList.add(MainActivity.Audio(contentURI,name,  album, title, artist, duration))
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
private fun getPlaylistNames(playlists : MutableMap<String, MutableList<Audio>>): List<String> {
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

/** Edit the playlist */
@Composable
fun Edit(playlist: MutableList<Audio>?) {

    var mExpanded by remember { mutableStateOf(false) }

    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}

    Button(onClick = { mExpanded = !mExpanded },
        modifier = Modifier
            .padding(start = 280.dp, top = 20.dp)
            .onGloballyPositioned { coordinates -> mTextFieldSize = coordinates.size.toSize() * 5F }) {
        Image(painter = painterResource(id = R.drawable.edit), contentDescription = null)
    }

    DropdownMenu(
        expanded = mExpanded,
        onDismissRequest = { mExpanded = false },
        modifier = Modifier
            .width(with(LocalDensity.current){mTextFieldSize.width.toDp()})
    ) {
        playlist?.forEach { song ->
            DropdownMenuItem(onClick = {
                // delete upon removal
                playlist.remove(song)
                mExpanded = false
            },
                text = { Text(text = song.name) }
            )
        }
    }
}

@Composable
fun AreYouSureAlert(songName : String, playlistName: String) : Boolean
{
    var delete = false
    var dismissed by remember {
        mutableStateOf(false)
    }
    if (!dismissed)
    {
        AlertDialog(
            modifier = Modifier.border(5.dp, color = Color.Red),
            onDismissRequest = { dismissed = true },
            confirmButton = { Text(text = "Yes"); delete = true; dismissed = true },
            dismissButton = { Text(text = "No"); delete = false; dismissed = true},
            text = {Text("Are you Sure?")},
            title = { Text(text = "Do you want to delete $songName from $playlistName")
            }
        )
    }
    else return delete
    return delete
}

/**
 * @param playlists
 * selectPlaylist(): Function to select playlist based off name.
 * */
@Composable
fun PlaylistSelect(playlists: MutableMap<String, MutableList<Audio>>, selectPlaylist: (String) -> Unit) {
    // dropdown menu for playlist select
    // Declaring a boolean value to store
    // the expanded state of the Text Field
    var mExpanded by remember { mutableStateOf(false) }

    // Create a list of cities
    val mPlaylist = getPlaylistNames(playlists)

    // Create a string value to store the selected city
    var mSelectedText by remember { mutableStateOf("") }

    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}

    var showCreation by remember {
        mutableStateOf(false)
    }

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
                    // set playlist (current playlist)
                    selectPlaylist(label)
                    mExpanded = false
                },
                    text = { Text(text = label) }
                )
            }

            // create new playlist button
            DropdownMenuItem(text = { Text(text = "Create New Playlist") }, onClick = { showCreation = true })
        }
    }

    /** Prompt the user to enter text and create a new playlist
     * Holy hell I am tired
     */
    if (showCreation) {

    }
}

// https://stackoverflow.com/questions/73455840/textfield-new-line-issue-in-alert-dialog-with-jetpack-compose
@Composable
fun CreatePlaylistAlert() {
    var showCreation by remember {
        mutableStateOf(false)
    }

    val text = remember { mutableStateOf("") }
    val textLength = remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = { showCreation = false },
        title = {
            Text(text = "Create new playlist?",)
        },
        text = { TextField(
            value = text.value,
            onValueChange = {
                if (it.length > 200) {
                    textLength.value = it.length
                    text.value = it
                }
            },
            )},
        confirmButton = { Button(onClick = { showCreation = false;})
            {
                // This is the text of the button
                Text(text = "Add Playlist")
            }
        },


    )
}



@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun Player(
    player: MediaPlayer,
    currentSong: MainActivity.Audio,
    context: Context,
    image: Bitmap,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    skipSong: () -> Unit,
    playlists : MutableMap<String, MutableList<Audio>>,
    selectPlaylist: (name: String) -> Unit,
    currentPlaylistName: String,
    previousSong: () -> Unit
) {
    var modifier = Modifier.fillMaxWidth()

    // Allows to control card like swiping
    val twyperController = rememberTwyperController()



    Column (
        modifier = Modifier.padding(top = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Playlist selector
        PlaylistSelect(playlists, selectPlaylist = {selectPlaylist(currentPlaylistName)})
        // song title (replace with song name variable

        SongTitle(title = currentSong.title)
        // Card swiping view
        CardSwipe(artist = currentSong.artist,
            image = image,
            twyperController = twyperController,
            onAccept =  { onAccept() },
            onReject = { onReject() },
            items = listOf(currentSong)
        )


        
        Spacer(modifier = Modifier.weight(1.0f))
        Spacer(modifier = Modifier.height(5.dp))

        // accept / reject button
        AcceptReject(
            onAccept = {
                println(currentSong)
                twyperController.swipeRight()
            },
            onReject = {
                twyperController.swipeLeft()
            }
        )
        var totalDuration = currentSong.duration.toLong()

        var currentTime by remember {
            mutableLongStateOf(player.currentPosition.toLong())
        }
        run {
            CoroutineScope(Dispatchers.IO).launch {
                // Reduce timeMillis to make smoother, but obv cost more on this thread)
                // 1s seems good to not make seeking all laggy
                delay(1000)
                try {
                    if (player.isPlaying)
                    {
                        currentTime = player.currentPosition.toLong()
                    }
                }
                catch (e: IllegalStateException) {
                    println("Trying to get player when doesn't exist: " + e)
                }

            }
        }
        // music progress bar
        TrackSlider(
            value = currentTime.toFloat(),
            onValueChange = {
                println(it)
                currentTime = it.toLong()
                // Needs to be 'previous' so if dragged to end doesn't crash
                if (player.duration > it.toLong()) {
                    player.seekTo(it.toLong(), MediaPlayer.SEEK_PREVIOUS_SYNC)

                }
                else {
                    currentTime = 0
                    player.seekTo(0)
                }

            },
            onValueChangeFinished = {
                //currentTime = sliderPosition.longValue
                //player.seekTo(sliderPosition.longValue.toInt())
            },
            songDuration = totalDuration.toFloat()
        )
        // music times
        var minutes = totalDuration / (60000)
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
        Playbar(
            currentSong,
            player,
            context,
            skipSong = { skipSong() },
            previousSong = { previousSong() })
    }


}

@Composable
fun SongTitle(title: String) {

    Text(
        text = title,
        modifier = Modifier
            .padding(top = 5.dp)
            ,
        color = Color.White,

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
fun CardSwipe(image: Bitmap, artist: String, twyperController: TwyperController,
              onAccept: () -> Unit,
              onReject: () -> Unit, items : List<Any>) {
    Twyper(items = items, twyperController = twyperController, onItemRemoved = {
            item, direction ->
        if (direction == SwipedOutDirection.LEFT) {
            println("Swiped Left: Rejecting")
            onReject()
        }
        else {
            println("Swiped Right: Accepting")
            onAccept()
        }
    }) {
        Column {
            MusicImage(image = image)

            ArtistName(name = artist)
        }
    }




    
}

@Composable
fun ArtistName(name: String) {
    Text(
        text = name,
        modifier = Modifier.padding(top = 5.dp)    )
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
        Text(startTime, style = TextStyle(color = MaterialTheme.colorScheme.primary))
        Text(endTime, style = TextStyle(color = MaterialTheme.colorScheme.primary))


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
fun Playbar(
    currentSong: Audio,
    mediaPlayer: MediaPlayer,
    context: Context,
    skipSong: () -> Unit,
    previousSong: () -> Unit
) {
    Row (
        modifier = Modifier
            .padding(horizontal = 20.dp)
            .padding(bottom = 75.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        PreviousButton(previousSong)
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
            onClick = { mediaPlayer.start(); playing = true  },
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
fun PreviousButton(PreviousSong : () -> Unit) {
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
fun toM3U(playlistName: String, playlist: MutableList<MainActivity.Audio>?, context: Context) : String {
    // grab a playlist
    var out: StringBuilder = StringBuilder()

    out.append("#EXTM3U\n")
    val path = Environment.getExternalStoragePublicDirectory("Music")
    if (playlist != null) {
        for (song in playlist) {
            out.append("#EXTINF:").append(song.duration / 1000).append(",").append(song.artist).append(" - ").append(song.name).append("\n")
            out.append(path).append("/").append(song.name).append("\n")
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
