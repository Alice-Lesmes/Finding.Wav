package com.example.findingwav

import android.media.MediaPlayer
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding

import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.materialIcon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import androidx.media3.exoplayer.ExoPlayer
import com.example.findingwav.ui.theme.FindingWavTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val player = ExoPlayer.Builder(this).build()

        setContent {
            FindingWavTheme {
                var getMusicClass = MusicPlayerTest()
                // Inits the musicplayer. rn is loonboon
                var musicPlayerTest = MediaPlayer.create(this, R.raw.loonboon)
                Scaffold(
                    modifier =

                    Modifier.fillMaxSize()
                ) { innerPadding ->
                    var showTime by remember {
                        mutableStateOf(false)
                    }
                    var currentTime by remember {
                        mutableStateOf(musicPlayerTest.timestamp)
                    }

                    Column {
                        Button(modifier = Modifier.padding(20.dp), onClick = {
                            // plays music, defined on the create
                            musicPlayerTest.start();
                            showTime = true
                            currentTime = musicPlayerTest.timestamp

                        }) {
                            Text(text = "play music")
                        }
                        if (showTime) {
                            // Only updates on button press but whatever
                            Text("Here is the current timestamp: " + currentTime)
                        }
                        Text(text = getMusicClass.getMusicDir().toString())
                    }


                    /* Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                * Elem()
                * } */
                    /* Surface (parameters) {
                *   Elem()
                * }*/
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(4.dp)
                    )
                    Scaffold(modifier = Modifier.fillMaxSize()) { padding ->
                        TestGreet(
                            x = "First Android App",
                            y = "Second App",
                            modifier = Modifier.padding(padding)
                        )

                    }
                    Player(player)
                }
            }
        }
    }
}
// this could be useful (making the basic music bar)
// https://www.digitalocean.com/community/tutorials/android-media-player-song-with-seekbar


@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var musicPlayerTest = MusicPlayerTest()

}

@Composable
fun TestGreet(x: String, y: String, modifier: Modifier = Modifier) {
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
            //modifier = Modifier
            //    .padding(top = 40.dp)

        )
    }
}

@Composable
fun PlaylistSelect() {
    // dropdown menu for playlist select
}

@Composable
fun Player(player: ExoPlayer) {
    var modifier = Modifier.fillMaxWidth()


    Column (
        modifier = Modifier.padding(top = 150.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // song title
        SongTitle()
        // music image
        MusicImage()
        // artist name
        ArtistName()
        // accept / reject button
        AcceptReject()
        // music progress bar

        //
        val sliderPosition = remember {
            mutableLongStateOf(0)
        }

        val currentPosition = remember {
            mutableLongStateOf(0)
        }

        val totalDuration = remember {
            mutableLongStateOf(0)
        }

        TrackSlider(
            value = sliderPosition.longValue.toFloat(),
            onValueChange = {
                sliderPosition.longValue = it.toLong()
            },
            onValueChangeFinished = {
                currentPosition.longValue = sliderPosition.longValue
                player.seekTo(sliderPosition.longValue)
            },
            songDuration = totalDuration.longValue.toFloat()
        )
        // music controls
        Playbar()
    }
}

@Composable
fun SongTitle(modifier: Modifier = Modifier) {
    Text(
        text = "Song Title"
    )
}

@Composable
fun MusicImage() {
    val image = painterResource(id = R.drawable.musik)
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
fun ArtistName() {
    Text(
        text = "Artist Name"
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

// should you make a button take an image
// or should you make an image clickable?

@Composable
fun Accept() {
    Button(onClick = { /*TODO*/ },
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
        onClick = { /*TODO*/ },
        colors = ButtonColors(Color.Red, Color.Red, Color.Red, Color.Red),
        modifier = Modifier
            .width(70.dp)
            .height(70.dp)
    ) {
        Image(painter = painterResource(id = R.drawable.reject), contentDescription = null)
        // make bg colour green
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
        )
    )
}


@Composable
fun Playbar() {
    Row (
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
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
        onClick = { /*TODO*/ },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
    ) {
        Image(painter = painterResource(id = R.drawable.play), contentDescription = null)
    }
}

@Composable
fun PreviousButton() {
    Button(
        onClick = { println("bonjour") },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
        ) {
        Image(painter = painterResource(id = R.drawable.previous), contentDescription = null)
    }
}

@Composable
fun NextButton() {
    Button(
        onClick = { /*TODO*/ },
        colors = ButtonDefaults.buttonColors(containerColor = Color.White.copy(alpha = 0.0F))
    ) {
        Image(painter = painterResource(id = R.drawable.next), contentDescription = null)

    }
}

// ****************
// START OF PREVIEWS
// ****************

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FindingWavTheme {
        Greeting("Android")
    }
}

@Preview(showBackground = true)
@Composable
fun TestGreetPreview() {
    FindingWavTheme {
        TestGreet("First Android App", "Second App")
    }
}

@Preview(showBackground = true)
@Composable
fun MusicImagePreview() {
    FindingWavTheme {
        MusicImage()
    }
}
