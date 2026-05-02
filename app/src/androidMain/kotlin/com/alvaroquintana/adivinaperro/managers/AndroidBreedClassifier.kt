package com.alvaroquintana.adivinaperro.managers

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import com.alvaroquintana.domain.BreedPrediction
import org.tensorflow.lite.Interpreter
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel

class AndroidBreedClassifier(
    context: Context,
    private val mapper: LabelBreedMapper
) : BreedClassifier {

    private val labels: List<String> by lazy {
        context.assets.open("breed_labels.txt").bufferedReader().readLines()
    }

    private val interpreter: Interpreter by lazy {
        val assetFd = context.assets.openFd("breed_model.tflite")
        val modelBuffer = assetFd.createInputStream().channel.map(
            FileChannel.MapMode.READ_ONLY,
            assetFd.startOffset,
            assetFd.declaredLength
        )
        Interpreter(modelBuffer)
    }

    override fun isAvailable(): Boolean = true

    override suspend fun classify(imageBytes: ByteArray): List<BreedPrediction> {
        val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
            ?: return emptyList()
        return classifyBitmap(bitmap)
    }

    private suspend fun classifyBitmap(original: Bitmap): List<BreedPrediction> {
        val scaled = Bitmap.createScaledBitmap(original, INPUT_SIZE, INPUT_SIZE, true)
        val inputBuffer = bitmapToByteBuffer(scaled)
        val output = Array(1) { FloatArray(labels.size) }

        interpreter.run(inputBuffer, output)

        val scores = output[0]
        val rawLabels = scores
            .mapIndexed { index, score -> labels[index] to score }
            .sortedByDescending { it.second }
            .take(MAX_RESULTS)

        return mapper.map(rawLabels)
    }

    private fun bitmapToByteBuffer(bitmap: Bitmap): ByteBuffer {
        val buffer = ByteBuffer.allocateDirect(4 * INPUT_SIZE * INPUT_SIZE * CHANNELS)
        buffer.order(ByteOrder.nativeOrder())
        val pixels = IntArray(INPUT_SIZE * INPUT_SIZE)
        bitmap.getPixels(pixels, 0, INPUT_SIZE, 0, 0, INPUT_SIZE, INPUT_SIZE)

        for (pixel in pixels) {
            val r = (pixel shr 16 and 0xFF) / 255.0f
            val g = (pixel shr 8 and 0xFF) / 255.0f
            val b = (pixel and 0xFF) / 255.0f
            buffer.putFloat(r)
            buffer.putFloat(g)
            buffer.putFloat(b)
        }
        buffer.rewind()
        return buffer
    }

    companion object {
        private const val INPUT_SIZE = 224
        private const val CHANNELS = 3
        private const val MAX_RESULTS = 5
    }
}
