package com.example.findingwav


import android.R.attr.path
import android.content.Context
import android.database.Cursor
import android.provider.MediaStore
import androidx.appcompat.app.AppCompatActivity
import java.io.File


class MusicPlayerTest : AppCompatActivity() {


    fun getMusicDir() {
        val directoryPath = "exampleDir" // Replace with your directory path
        val directory = File(directoryPath)

        val files = directory.listFiles()?.filter { it.isFile }
        files?.forEach { file ->
            println(file.name)
        } ?: println("No files found or directory does not exist.")

    }
}