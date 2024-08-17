package com.example.findingwav

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
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
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
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