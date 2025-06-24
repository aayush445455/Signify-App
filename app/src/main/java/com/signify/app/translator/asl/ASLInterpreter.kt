// File: app/src/main/java/com/signify/app/translator/asl/ASLInterpreter.kt
package com.signify.app.translator.asl

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class ASLInterpreter(context: Context) {
    private val interpreter: Interpreter

    init {
        // load the tflite from assets root
        val modelBuffer = loadModelFile(context, "asl_mlp_final.tflite")
        interpreter = Interpreter(modelBuffer)
    }

    fun predict(features: FloatArray): Int {
        val output = Array(1) { FloatArray(36) }  // adjust size to your model’s output
        interpreter.run(features, output)
        return output[0].indices.maxByOrNull { output[0][it] } ?: -1
    }

    private fun loadModelFile(context: Context, assetName: String): MappedByteBuffer {
        context.assets.openFd(assetName).use { afd ->
            FileInputStream(afd.fileDescriptor).channel.use { channel ->
                return channel.map(
                    FileChannel.MapMode.READ_ONLY,
                    afd.startOffset,
                    afd.length
                )
            }
        }
    }
}
