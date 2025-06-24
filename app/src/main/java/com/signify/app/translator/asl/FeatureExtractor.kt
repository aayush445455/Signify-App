// File: app/src/main/java/com/signify/app/translator/asl/FeatureExtractor.kt
package com.signify.app.translator.asl

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.acos
import kotlin.math.pow
import kotlin.math.sqrt

object FeatureExtractor {
    // Each hand has 21 landmarks; we flatten (x,y,z) → 63 values
    // then add 20 bone lengths, 2 reference lengths, 15 angles, 1 depth-variance = 101 features.
    fun extractFeatures(landmarks: List<NormalizedLandmark>): FloatArray {
        // 1) raw coords array [21*3]
        val coords = FloatArray(landmarks.size * 3)
        for (i in landmarks.indices) {
            coords[i * 3]     = landmarks[i].x()   // <-- use x()
            coords[i * 3 + 1] = landmarks[i].y()   // <-- use y()
            coords[i * 3 + 2] = landmarks[i].z()   // <-- use z()
        }

        // 2) normalize around wrist (idx=0)
        val wristX = coords[0]
        val wristY = coords[1]
        val wristZ = coords[2]
        // reference length = distance wrist→middle fingertip (idx=12)
        val ref = euclid(
            coords[12*3]    - wristX,
            coords[12*3+1]  - wristY,
            coords[12*3+2]  - wristZ
        ).coerceAtLeast(1e-6f)
        for (i in coords.indices) {
            coords[i] = (coords[i] - when (i % 3) {
                0 -> wristX
                1 -> wristY
                else -> wristZ
            }) / ref
        }

        // 3) gather features:
        val feats = mutableListOf<Float>()

        // 3a) flattened normalized coords
        feats += coords.toList()

        // helper for distance
        fun dist(i: Int, j: Int): Float {
            val dx = coords[i*3]   - coords[j*3]
            val dy = coords[i*3+1] - coords[j*3+1]
            val dz = coords[i*3+2] - coords[j*3+2]
            return euclid(dx, dy, dz)
        }

        // five fingers as index chains
        val FINGERS = listOf(
            listOf(0,1,2,3,4),
            listOf(0,5,6,7,8),
            listOf(0,9,10,11,12),
            listOf(0,13,14,15,16),
            listOf(0,17,18,19,20),
        )

        // 3b) bone lengths
        for (finger in FINGERS) {
            for (k in 0 until finger.size-1) {
                feats += dist(finger[k], finger[k+1])
            }
        }

        // 3c) two reference lengths
        feats += dist(1, 17)  // index-tip ↔ pinky-tip
        feats += dist(0, 12)  // wrist ↔ middle-tip

        // 3d) joint angles
        fun angle(a: Int, b: Int, c: Int): Float {
            val v1x = coords[a*3]   - coords[b*3]
            val v1y = coords[a*3+1] - coords[b*3+1]
            val v1z = coords[a*3+2] - coords[b*3+2]
            val v2x = coords[c*3]   - coords[b*3]
            val v2y = coords[c*3+1] - coords[b*3+1]
            val v2z = coords[c*3+2] - coords[b*3+2]
            val dot = v1x*v2x + v1y*v2y + v1z*v2z
            val mag1 = euclid(v1x, v1y, v1z)
            val mag2 = euclid(v2x, v2y, v2z)
            return acos((dot / ((mag1*mag2).coerceAtLeast(1e-6f))).coerceIn(-1f,1f))
        }
        for (finger in FINGERS) {
            for (k in 0 until finger.size-2) {
                feats += angle(finger[k], finger[k+1], finger[k+2])
            }
        }

        // 3e) depth (z-axis) variance
        val zs = coords.slice(2 until coords.size step 3)
        val meanZ = zs.sum() / zs.size
        feats += zs.fold(0f) { acc, v -> acc + (v-meanZ).pow(2) } / zs.size

        return feats.toFloatArray()
    }

    private fun euclid(x: Float, y: Float, z: Float) =
        sqrt(x*x + y*y + z*z)
}
