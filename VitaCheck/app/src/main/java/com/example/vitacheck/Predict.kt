package com.example.vitacheck

import android.content.Context
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import java.nio.ByteBuffer

@Composable
fun PredictionScreen(context: Context, image: Uri) {
    val prediction = remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        prediction.value = predict(context, image)
    }

    Column (
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        prediction.value?.let {
            Text(text = "Prediction: ${prediction.value}")
//            Text(text = "Confidence: ${"%.2f".format(it.second * 100)}%")
        } ?: Text(text = "Loading...")
    }
}
