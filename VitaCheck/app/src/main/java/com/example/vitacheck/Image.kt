package com.example.vitacheck

import android.content.Context
import android.graphics.Bitmap
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier

@Composable
fun ImagePredictionApp(context: Context) {
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val imageBuffer = imageBitmap.value?.let { preprocessImage(it) }

    Column(modifier = Modifier.fillMaxSize()) {
        Button(onClick = {
            // Implement image picker or camera capture
        }) {
            Text(text = "Select Image")
        }

        imageBuffer?.let {
            PredictionScreen(context, it)
        } ?: Text(text = "Please select an image.")
    }
}
