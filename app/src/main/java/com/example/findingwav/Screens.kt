//package com.example.findingwav
//
//import android.os.Build
//import androidx.annotation.RequiresApi
//import androidx.compose.runtime.Composable
//import com.example.findingwav.ui.screens.PlayerScreen
//
//interface Screen {
//    val name : String
//    val screen: @Composable () -> Unit
//}
//
//object Main : Screen {
//    override val name: String = "Main"
//    @RequiresApi(Build.VERSION_CODES.Q)
//    override val screen: @Composable (() -> Unit) = { PlayerScreen() }
//}