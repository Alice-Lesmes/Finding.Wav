package com.example.findingwav

import android.media.MediaPlayer
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.findingwav.ui.theme.FindingWavTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindingWavTheme {

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
                    }




                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    var musicPlayerTest = MusicPlayerTest()
    Button(onClick = { musicPlayerTest.playMusic() }) {
        Text(text = "Play Music")
    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    FindingWavTheme {
        Greeting("Android")
    }
}