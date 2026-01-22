package com.example.findingwav.ui.screens

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.OptIn
import androidx.annotation.RequiresApi
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.unit.toSize
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.toBitmap
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
import androidx.media3.common.util.UnstableApi
import com.example.findingwav.MusicPlayer
import com.example.findingwav.NextOpts
import com.example.findingwav.R
import com.example.findingwav.toM3U
import com.example.findingwav.ui.theme.FindingWavTheme
import com.github.theapache64.twyper.SwipedOutDirection
import com.github.theapache64.twyper.Twyper
import com.github.theapache64.twyper.TwyperController
import com.github.theapache64.twyper.rememberTwyperController
import kotlinx.coroutines.delay


@RequiresApi(Build.VERSION_CODES.Q)
@Composable
fun PlayerScreen(musicPlayer : MusicPlayer, context : Context) {
    FindingWavTheme {
        Scaffold(modifier =

            Modifier.fillMaxSize()) { innerPadding ->
        }
//        occasionally

        // main ui
        Row(
            horizontalArrangement = Arrangement.spacedBy(2.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(top=5.dp)
        ) {

            // Added duration as individual parameter to avoid using deprecated MediaMetaData.durationMS
            Export(
                musicPlayer.getCurrentPlaylistName(),
                musicPlayer.getPlaylist(musicPlayer.getCurrentPlaylistName()),
                context
            )
            Title("Finding Wuv", "Playlist Creation Mode", Modifier)
            // Added duration as individual parameter to avoid using deprecated MediaMetaData.durationMS

            Edit(musicPlayer.getPlaylist(musicPlayer.getCurrentPlaylistName()))
        }

        val currentSong = remember {
            mutableStateOf(MediaItem.Builder().build())
        }
        val currentSongMetadata = remember {
            mutableStateOf(MediaItem.Builder().build().mediaMetadata)
        }
        currentSong.value = musicPlayer.getCurrentSong(false)!!
        currentSongMetadata.value = currentSong.value.mediaMetadata

        // Idk what this really does, uh, god help us all
        Player(
            musicPlayer,
            context,
            onChange = {
                currentSong.value = musicPlayer.player.currentMediaItem!!
            },
            onAccept = {
                currentSong.value = musicPlayer.getCurrentSong(false)!!
            },
            onReject = {
                currentSong.value = musicPlayer.getCurrentSong(false)!!

            },
            musicPlayer.getPlaylists(),
            selectPlaylist = { musicPlayer.setCurrentPlaylist(musicPlayer.getCurrentPlaylistName()) },
            musicPlayer.getCurrentPlaylistName()
        )
    }
}


//@Composable
//fun settings(onClick: (screen)) {
//    Button(onClick = {
//            screen -> currentScreen
//    }) { }
//}

/** Edit the playlist */
@Composable
private fun Edit(playlist: MutableList<MediaItem>?) {

    var mExpanded by remember { mutableStateOf(false) }

    var mTextFieldSize by remember { mutableStateOf(Size.Zero)}

    Button(onClick = { mExpanded = !mExpanded },
        modifier = Modifier
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
                text = { Text(text = song.mediaMetadata.title.toString()) }
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
    return delete
}

@Composable
private fun SettingsSelect() {

}

/**
 * @param playlists
 * selectPlaylist(): Function to select playlist based off name.
 * */
@Composable
private fun PlaylistSelect(playlists: MutableMap<String, MutableList<MediaItem>>, selectPlaylist: (String) -> Unit) {
    // dropdown menu for playlist select
    // Declaring a boolean value to store
    // the expanded state of the Text Field
    var mExpanded by remember { mutableStateOf(false) }

    // Create a list of cities

    val mPlaylist = playlists.keys

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
private fun CreatePlaylistAlert() {
    var showCreation by remember {
        mutableStateOf(false)
    }

    val text = remember { mutableStateOf("") }
    val textLength = remember { mutableStateOf(0) }

    AlertDialog(
        onDismissRequest = { showCreation = false },
        title = {
            Text(text = "Create new playlist?")
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


@OptIn(UnstableApi::class)
@RequiresApi(Build.VERSION_CODES.Q)
@Composable
private fun Player(
    musicPlayer: MusicPlayer,
    context: Context,
    onChange: () -> Unit,
    onAccept: () -> Unit,
    onReject: () -> Unit,
    playlists: MutableMap<String, MutableList<MediaItem>>,
    selectPlaylist: (name: String) -> Unit,
    currentPlaylistName: String) {
    var modifier = Modifier.fillMaxWidth()

    // Allows to control card like swiping
    val twyperController = rememberTwyperController()

    // TODO: idk if this is necessary, however imma add a TODO here to double check if we do later
    val currentSongMetadata = remember {
        mutableStateOf(musicPlayer.getCurrentSong(false)!!.mediaMetadata)
    }

    LaunchedEffect(currentSongMetadata) {
        currentSongMetadata.value = musicPlayer.getCurrentSong(false)!!.mediaMetadata
    }

    val isPlaying = remember {
        mutableStateOf(false)
    }
    // Following this guide for this stuff:
    // https://alitalhacoban.medium.com/build-music-player-with-jetpack-compose-media3-exoplayer-cf3d44a0a67a
    val currentPosition = remember {
        mutableLongStateOf(0)
    }
    val sliderPosition = remember {
        mutableLongStateOf(currentPosition.longValue)
    }
    val totalDuration = remember {
        mutableLongStateOf(musicPlayer.getCurrentSong(false)!!.mediaMetadata.durationMs!!)
    }
    // Personally, I'd rather not have these things  here, i can move them later TODO:
    /**
     * Whenever the song changes set the new metadata values correctly.
     * This is done to prevent naturally completing a song but not having the title, and other stuff change
     */
    musicPlayer.player.addListener(object: Player.Listener {
        override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
            super.onMediaItemTransition(mediaItem, reason)
            currentSongMetadata.value = musicPlayer.getCurrentSong(false)!!.mediaMetadata
            currentPosition.longValue = 0
            sliderPosition.longValue = 0
            totalDuration.longValue = currentSongMetadata.value.durationMs!!
        }
        /*
        Stupid having to make this functionality, but it's more consistent than player.isPlaying
        */
        override fun onIsPlayingChanged(playing: Boolean) {
            super.onIsPlayingChanged(playing)
            isPlaying.value = playing
        }
    })

    var image: Bitmap? = null
    try {
        // try load from contenturi instead of artwork uri
        val hiddenUriString = currentSongMetadata.value.extras?.getString("raw_file_uri")

        val uriToLoad = if (hiddenUriString != null) {
            android.net.Uri.parse(hiddenUriString)
        } else {
            currentSongMetadata.value.artworkUri
        }

        // 3. Load whichever one we found
        image = uriToLoad?.let {
            context.contentResolver.loadThumbnail(it, android.util.Size(512, 512), null)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        // 'image' remains null here, which triggers your existing fallback logic later
    }
    // Fallback if image failed to load OR was null
    if (image == null) {
        val noImageDrawable = ContextCompat.getDrawable(context, R.drawable.noimage)
        image = noImageDrawable?.toBitmap(512, 512)
    }


    Column (
        modifier = Modifier.padding(top = 110.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Playlist selector
        PlaylistSelect(playlists, selectPlaylist = {selectPlaylist(currentPlaylistName)})
        // song title (replace with song name variable

        SongTitle(title = currentSongMetadata.value.title.toString())
        // Card swiping view
        CardSwipe(
            artist = currentSongMetadata.value.artist.toString(),
            image = image!!,
            twyperController = twyperController,
            onAccept =  {
                onAccept()
                musicPlayer.nextSong(NextOpts.FORCEADD)
//                musicPlayer.player.seekToNextMediaItem()
                currentSongMetadata.value = musicPlayer.getCurrentSong(false)!!.mediaMetadata
            },
            onReject = {
                musicPlayer.nextSong(NextOpts.DONTADD)
 //               musicPlayer.player.seekToNextMediaItem()
                currentSongMetadata.value = musicPlayer.getCurrentSong(false)!!.mediaMetadata

            },
            items = listOf(currentSongMetadata.value)
        )



        Spacer(modifier = Modifier.weight(1.0f))
        Spacer(modifier = Modifier.height(5.dp))

        // accept / reject button
        AcceptReject(
            onAccept = {
                twyperController.swipeRight()
            },
            onReject = {
                twyperController.swipeLeft()
            }
        )


        // currentPosition.longValue = musicPlayer.player.currentPosition
        // I think the point of the LaunchedEffects is to make sure that the thing is in the right thread (main)
        LaunchedEffect(key1 = musicPlayer.player.currentPosition, key2 = isPlaying.value) {
            delay(1000)
            currentPosition.longValue = musicPlayer.player.currentPosition
            musicPlayer.previousSongPlayTime(currentPosition.longValue)
        }

        LaunchedEffect(key1 = musicPlayer.player.isPlaying(), key2 = !musicPlayer.player.isPlaying()) {
            isPlaying.value = musicPlayer.player.isPlaying()
        }

        LaunchedEffect(sliderPosition) {
            sliderPosition.longValue = currentPosition.longValue
        }

        LaunchedEffect(musicPlayer.player.duration) {
            if (musicPlayer.player.duration > -1) {
                totalDuration.longValue = musicPlayer.getCurrentSong(false)!!.mediaMetadata.durationMs!!
            }
        }

        TrackSlider(
            value = currentPosition.longValue.toFloat(),
            onValueChange = {
                // No clue why sometimes it works without this line first
                sliderPosition.longValue = it.toLong()
                // Sometimes works with just it.toLong(), but sometimes not
                currentPosition.longValue = sliderPosition.longValue
            },
            onValueChangeFinished = {
                // Again, no clue why this is required, but whatever
                currentPosition.longValue = currentPosition.longValue
                musicPlayer.player.seekTo(currentPosition.longValue)

            },
            songDuration = totalDuration.longValue.toFloat()
        )
        // music times
        var minutes = totalDuration.value / (60000)
        var seconds = (totalDuration.longValue / 1000) % 60
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
            musicPlayer.player,
            isPlaying.value,
            play = { isPlaying.value = true; musicPlayer.player.play() },
            pause = { isPlaying.value = false; musicPlayer.player.pause() },
            skipSong = {
                musicPlayer.nextSong(NextOpts.NORMAL)
                //musicPlayer.player.seekToNextMediaItem()
                currentSongMetadata.value = musicPlayer.getCurrentSong(false)!!.mediaMetadata
            },
            previousSong = {
                musicPlayer.previousSong(NextOpts.NORMAL)
//                musicPlayer.player.seekToPreviousMediaItem()
                currentSongMetadata.value = musicPlayer.getCurrentSong(false)!!.mediaMetadata
            })
    }
}

@Composable
private fun SongTitle(title: String) {

    Text(
        text = title,
        modifier = Modifier
            .padding(top = 5.dp),
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
fun CardSwipe(
    image: Bitmap, artist: String, twyperController: TwyperController,
    onAccept: () -> Unit,
    onReject: () -> Unit, items: List<Any>) {
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
        Column (
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                    .fillMaxWidth(0.8f)
        ) {
            MusicImage(image = image)

            ArtistName(name = artist)
        }
    }





}

@Composable
fun ArtistName(name: String) {
    Text(
        text = name,
        textAlign = TextAlign.Center,
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
    mediaPlayer: Player,
    playing: Boolean,
    play: () -> Unit,
    pause: () -> Unit,
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
        PlayButton(mediaPlayer, playing, play, pause)
        NextButton(skipSong)
    }
}

@Composable
fun PlayButton(mediaPlayer: Player, playing : Boolean, play : () -> Unit, pause: () -> Unit) {
    if (!playing)
    {
        Button(
            //TODO: Make sure this .play() doesn't cause an error since it isn't prepared
            // it shouldn't since the player should have a loaded playlist
            onClick = {  play()/*mediaPlayer.play(); playing = true*/  },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
        ) {
            Image(painter = painterResource(id = R.drawable.play), contentDescription = null)
        }
    }
    else
    {
        Button(
            onClick = { pause() /*mediaPlayer.pause(); playing = false*/  },
            colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
        ) {
            Image(painter = painterResource(id = R.drawable.pause), contentDescription = null, contentScale = ContentScale.FillBounds )
        }
    }

}


@Composable
fun PreviousButton(PreviousSong : () -> Unit) {
    Button(
        onClick = { PreviousSong();  },
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



@Composable
fun Title(x: String, y: String, modifier: Modifier = Modifier) {
    // the row is not row-ing
    Column(
        modifier = Modifier
            .fillMaxWidth(0.7f),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,

        ) {
        Text(
            text = x,
            // does... something...
            fontSize = 20.sp,  // specify size
            color = MaterialTheme.colorScheme.primary,
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
            fontSize = 15.sp,
            color = MaterialTheme.colorScheme.secondary,
            textAlign = TextAlign.Center,
            // lineHeight = 10.sp,
            modifier = Modifier
                .padding(bottom = 10.dp)
        )
    }
}

/** Export the current playlist */
@Composable
fun Export(playlistName: String, playlist: MutableList<MediaItem>?, context: Context) {
    Button(
        onClick = { toM3U(playlistName, playlist, context) },
    ) {
        Image(
            painter = painterResource(id = R.drawable.export),
            contentDescription = null,
        )
    }

}

