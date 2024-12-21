package com.example.vitacheck

import android.graphics.Bitmap
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest


@Composable
fun ImagePredictionApp() {
    val context = LocalContext.current
    val contentResolver = context.contentResolver
    val imageBitmap = remember { mutableStateOf<Bitmap?>(null) }
    val imageUri = remember { mutableStateOf<Uri?>(null) }
//    val imageBuffer = imageUri.value?.let { preprocessImageUri(contentResolver,it) }

    val galleryLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) {uri ->
        imageUri.value = uri
        imageBitmap.value = null
//        imageBuffer = uri?.let { preprocessImage(contentResolver, it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(contract = ActivityResultContracts.TakePicturePreview()) {
        imageBitmap.value = it
        imageUri.value = null
    }

    val cameraPer = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {isGranted: Boolean ->
        if(isGranted){
            cameraLauncher.launch(null)
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    val galleryPer = rememberLauncherForActivityResult(contract = ActivityResultContracts.RequestPermission()) {isGranted: Boolean ->
        if(isGranted){
            galleryLauncher.launch("image/*")
        } else {
            Toast.makeText(context, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .height(300.dp)
                .background(Color.LightGray, RoundedCornerShape(8.dp)),
            shape = RoundedCornerShape(8.dp),
            color = Color.White
        ) {
            if (imageBitmap.value != null || imageUri.value != null) {
                if (imageUri.value != null) {
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(imageUri.value)
                            .build(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else if (imageBitmap.value != null) {
                    Image(
                        bitmap = imageBitmap.value!!.asImageBitmap(),
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            } else {
                Text(
                    text = "No Image Selected",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 10.dp)
                )
            }
        }
        Row(
            Modifier
                .fillMaxWidth(),
//            verticalAlignment = Alignment.Bottom,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = {
                // Implement image picker or camera capture
                cameraPer.launch(android.Manifest.permission.CAMERA)
            }) {
                Text(text = "Select Camera")
            }

            Button(onClick = {
                // Implement image picker or camera capture
//                galleryPer.launch(android.Manifest.permission.READ_EXTERNAL_STORAGE)
                galleryLauncher.launch("image/*")
            }) {
                Text(text = "Select Gallery")
            }
        }

        imageUri.value?.let{
            PredictionScreen(context, it)
        } ?: Text(text = "Please Select an Image")
//        imageBuffer?.let {
//            PredictionScreen(context, it)
//        } ?: Text(text = "Please select an image.")
    }
}
