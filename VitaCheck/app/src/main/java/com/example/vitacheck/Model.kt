@file:Suppress("DEPRECATION")

package com.example.vitacheck
import android.content.ContentResolver
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.provider.MediaStore
import org.tensorflow.lite.DataType
import org.tensorflow.lite.Interpreter
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.nio.ByteOrder

fun loadLabelsFromJson(context: Context, fileName: String): List<String> {
    val json = context.assets.open(fileName).bufferedReader().use { it.readText() }
    val gson = Gson()
    return gson.fromJson(json, object : TypeToken<List<String>>() {}.type)
}

fun loadModelFile(context: Context, modelName: String): MappedByteBuffer {
    val fileDescriptor = context.assets.openFd(modelName)
    val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
    val fileChannel = inputStream.channel
    return fileChannel.map(FileChannel.MapMode.READ_ONLY, fileDescriptor.startOffset, fileDescriptor.declaredLength)
}

//fun loadLabels(context: Context, fileName: String): List<String> {
//    return context.assets.open(fileName).bufferedReader().readLines()
//}

//fun predict(context: Context, image: ByteBuffer): Pair<String, Float>? {
//    // Load model
//    val modelBuffer = loadModelFile(context, "model_grape.tflite")
//    val interpreter = Interpreter(modelBuffer)
//
//    // Allocate input/output buffers
//    val inputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 50, 50, 3), DataType.FLOAT32)
//    inputTensor.loadBuffer(image)
//
//    val outputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 3), DataType.FLOAT32)
//    interpreter.run(inputTensor.buffer, outputTensor.buffer)
//
//    // Get predictions
//    val predictions = outputTensor.floatArray
//    val labels = loadLabelsFromJson(context, "labels.json") // Ensure this returns a List<String> or Map<Int, String>
//
//    // Find the label with the highest probability
//    val maxIndex = predictions.indices.maxByOrNull { predictions[it] } ?: return null
//    val predictedLabel = labels[maxIndex]  // Adjust this to access the label from the map or list
//    return predictedLabel to predictions[maxIndex]
//}

fun predict(context: Context, imageUri: Uri): String {
    try {
        // Load the model and interpreter
        val modelBuffer = loadModelFile(context, "model_grape.tflite")
        val interpreter = Interpreter(modelBuffer)

        // Load labels
        val labels = loadLabelsFromJson(context, "labels.json")

        // Load the image from URI
        val contentResolver = context.contentResolver
        val imageBitmap = loadBitmapFromUri(contentResolver, imageUri) ?: return "Error loading image"

        // Preprocess the image
        val imageBuffer = preprocessImage(imageBitmap)

        // Allocate input/output buffers
        val inputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 50, 50, 3), DataType.FLOAT32)
        inputTensor.loadBuffer(imageBuffer)

        val outputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 3), DataType.FLOAT32)
        interpreter.run(inputTensor.buffer, outputTensor.buffer)

        // Get predictions
        val predictions = outputTensor.floatArray
        val predictedClass = predictions.indices.maxByOrNull { predictions[it] } ?: return "Error predicting class"

        // Get the predicted label
        val predictedLabel = labels[predictedClass]

        return "Predicted Class: $predictedLabel"

    } catch (e: Exception) {
        e.printStackTrace()
        return "Error during prediction ${e.cause}"
    }
}

fun preprocessImage(imageBitmap: Bitmap): ByteBuffer {
    val scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, 50, 50, true)
    val buffer = ByteBuffer.allocateDirect(4 * 50 * 50 * 3)
    buffer.order(ByteOrder.nativeOrder())

    val intValues = IntArray(50 * 50)
    scaledBitmap.getPixels(intValues, 0, 50, 0, 0, 50, 50)
    for (pixel in intValues) {
        buffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f))
        buffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f))
        buffer.putFloat(((pixel and 0xFF) / 255.0f))
    }
    return buffer
}

fun loadBitmapFromUri(contentResolver: ContentResolver, uri: Uri): Bitmap? {
    return try {
        // Decode the image from the URI into a Bitmap
        MediaStore.Images.Media.getBitmap(contentResolver, uri)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

// Function to preprocess image (for both Bitmap and URI)
fun preprocessImageUri(contentResolver: ContentResolver, imageUri: Uri): ByteBuffer? {
    // Load Bitmap from URI
    val imageBitmap = loadBitmapFromUri(contentResolver, imageUri)
    if (imageBitmap == null) {
        return null
    }

    // Resize image to 50x50
    val width = 50
    val height = 50
    val channels = 3 // RGB
    val buffer = ByteBuffer.allocateDirect(4 * width * height * channels)
    buffer.order(ByteOrder.nativeOrder())

    // Resize the bitmap to 50x50
    val scaledBitmap = Bitmap.createScaledBitmap(imageBitmap, width, height, true)
    val intValues = IntArray(width * height)
    scaledBitmap.getPixels(intValues, 0, width, 0, 0, width, height)

    // Iterate through the pixels and put them in the buffer
    for (pixel in intValues) {
        buffer.putFloat(((pixel shr 16 and 0xFF) / 255.0f))  // Red
        buffer.putFloat(((pixel shr 8 and 0xFF) / 255.0f))   // Green
        buffer.putFloat(((pixel and 0xFF) / 255.0f))         // Blue
    }

    return buffer
}