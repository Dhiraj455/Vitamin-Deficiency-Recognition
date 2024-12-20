package com.example.vitacheck
import android.content.Context
import android.graphics.Bitmap
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

fun predict(context: Context, image: ByteBuffer): Pair<String, Float>? {
    // Load model
    val modelBuffer = loadModelFile(context, "model_grape.tflite")
    val interpreter = Interpreter(modelBuffer)

    // Allocate input/output buffers
    val inputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 50, 50, 3), DataType.FLOAT32)
    inputTensor.loadBuffer(image)

    val outputTensor = TensorBuffer.createFixedSize(intArrayOf(1, 3), DataType.FLOAT32)
    interpreter.run(inputTensor.buffer, outputTensor.buffer)

    // Get predictions
    val predictions = outputTensor.floatArray
    val labels = loadLabelsFromJson(context, "labels.json")
    val maxIndex = predictions.indices.maxByOrNull { predictions[it] } ?: return null
    return labels[maxIndex] to predictions[maxIndex]
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

