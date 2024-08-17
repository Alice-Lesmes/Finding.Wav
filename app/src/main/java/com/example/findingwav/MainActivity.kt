package com.example.findingwav

import android.media.MediaPlayer
import android.os.Bundle
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
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue

import androidx.compose.ui.Alignment

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp
import com.example.findingwav.ui.theme.FindingWavTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindingWavTheme {
                var getMusicClass = MusicPlayerTest()
                // Inits the musicplayer. rn is loonboon
                var musicPlayerTest = MediaPlayer.create(this, R.raw.loonboon)
                Scaffold(modifier =

                Modifier.fillMaxSize()) { innerPadding ->
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
                        if (showTime)
                        {
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
                        modifier = Modifier.padding(padding))

                }
                MusicImage()
                Playbar()
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
fun Player() {
    var modifier = Modifier.fillMaxWidth()

    Column() {
        // song title

        // music image

        // artist name

        // accept / reject button

        // music playing bar
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
            .padding(top = 200.dp)
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
fun Playbar() {
    Row (
        modifier = Modifier.fillMaxWidth()
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
    Button(onClick = { /*TODO*/ }) {
        Text(text = "")
    }
}

@Composable
fun PreviousButton() {
    Button(onClick = { /*TODO*/ }) {
        Text(text = "")
    }
}

@Composable
fun NextButton() {
    Button(onClick = { /*TODO*/ }) {
        Text(text = "")
    }
}


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