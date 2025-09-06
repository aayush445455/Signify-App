package com.signify.app.translator.asl

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class DynamicASLInterpreter(private val context: Context) {

    companion object {
        private const val MODEL_FILE = "asl_tcn_lstm.tflite"
        private val CLASS_LIST = listOf(
            "again","ask","because","big","blue","but","can","cannot","child",
            "close","come","drink","eat","enjoy","family","far","find","get","give",
            "go","good","goodbye","hello","help","know","learn","like","love","make"
        )

        private const val NUM_FRAMES = 16
        private const val MAX_FRAMES = 150
    }

    private val rawBuffer = mutableListOf<FloatArray>()
    private val interpreter = Interpreter(
        loadModel(),
        Interpreter.Options().setNumThreads(4)
    )

    fun processFrameRaw(raw138: FloatArray, onResult: (String) -> Unit) {
        rawBuffer.add(raw138)
        if (rawBuffer.size > MAX_FRAMES) rawBuffer.removeAt(0)

        // Only predict if we have enough frames for a sequence
        if (rawBuffer.size >= NUM_FRAMES) {
            // Use the most recent NUM_FRAMES
            val inputFrames = rawBuffer.takeLast(NUM_FRAMES)
            val dynamicSeq: Array<FloatArray> = computeDynamicFeatures(inputFrames, NUM_FRAMES)
            val input = arrayOf(dynamicSeq)
            val output = Array(1) { FloatArray(CLASS_LIST.size) }
            interpreter.run(input, output)

            val scores = output[0]
            val bestIdx = scores.indices.maxByOrNull { scores[it] } ?: -1
            val label = CLASS_LIST.getOrElse(bestIdx) { "?" }

            onResult(label)
        }
    }

    fun reset() {
        rawBuffer.clear()
    }

    private fun loadModel(): MappedByteBuffer {
        val afd = context.assets.openFd(MODEL_FILE)
        return FileInputStream(afd.fileDescriptor).channel.map(
            FileChannel.MapMode.READ_ONLY,
            afd.startOffset,
            afd.declaredLength
        )
    }
}
