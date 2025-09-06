package com.signify.app.translator.asl

import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import kotlin.math.acos
import kotlin.math.max
import kotlin.math.sqrt

// ---- FLATTEN LANDMARKS ----
/**
 * Flattens left/right hand (21 each) and pose (4) landmarks into a single FloatArray(138).
 * If any are missing, fills with zeros.
 */
fun flattenHolisticLandmarks(
    leftHand: List<NormalizedLandmark>?,
    rightHand: List<NormalizedLandmark>?,
    pose: List<NormalizedLandmark>?
): FloatArray {
    fun flatten(landmarks: List<NormalizedLandmark>?, count: Int): FloatArray =
        if (landmarks != null && landmarks.size == count)
            FloatArray(count * 3) { i ->
                when (i % 3) {
                    0 -> landmarks[i / 3].x()
                    1 -> landmarks[i / 3].y()
                    else -> landmarks[i / 3].z()
                }
            }
        else
            FloatArray(count * 3) { 0f }
    val left = flatten(leftHand, 21)
    val right = flatten(rightHand, 21)
    val pose4 = flatten(pose, 4)
    return left + right + pose4 // (63 + 63 + 12 = 138)
}

// ---- DYNAMIC SEQUENCE FEATURE PIPELINE ----

private val FINGERS = listOf(
    listOf(1,2,3,4),
    listOf(5,6,7,8),
    listOf(9,10,11,12),
    listOf(13,14,15,16),
    listOf(17,18,19,20)
)

/**
 * Compute 23 static features from one 138-dim frame:
 * Angles for both hands, elbow angles, and wrist distance.
 */
fun computeFrameFeatures(raw: FloatArray): FloatArray {
    // split:
    val leftRaw  = raw.sliceArray(0 until 63)
    val rightRaw = raw.sliceArray(63 until 126)
    val poseRaw  = raw.sliceArray(126 until 138)

    // helper: build a 21‐landmark list
    fun toLM(arr: FloatArray): List<Triple<Float,Float,Float>> =
        List(21) { i -> Triple(arr[i*3], arr[i*3+1], arr[i*3+2]) }
    val left  = toLM(leftRaw)
    val right = toLM(rightRaw)
    val pose4 = List(4) { i ->
        val base = i*3
        Triple(poseRaw[base], poseRaw[base+1], poseRaw[base+2])
    }

    // compute hand joint angles
    fun handAngles(hand: List<Triple<Float,Float,Float>>): List<Float> {
        val wrist = hand[0]
        val pw = run {
            val (x1,y1,_) = hand[5]; val (x2,y2,_) = hand[17]
            max(sqrt((x1-x2)*(x1-x2) + (y1-y2)*(y1-y2)), 1e-6f)
        }
        val coords = hand.map { (x,y,z) ->
            floatArrayOf((x-wrist.first)/pw, (y-wrist.second)/pw, (z-wrist.third)/pw)
        }
        val out = mutableListOf<Float>()
        for (chain in FINGERS) {
            for (i in 0 until chain.size-2) {
                val a = coords[chain[i]]; val b = coords[chain[i+1]]; val c = coords[chain[i+2]]
                val (v1x,v1y,v1z) = floatArrayOf(a[0]-b[0], a[1]-b[1], a[2]-b[2])
                val (v2x,v2y,v2z) = floatArrayOf(c[0]-b[0], c[1]-b[1], c[2]-b[2])
                val dot  = v1x*v2x + v1y*v2y + v1z*v2z
                val mag1 = max(sqrt(v1x*v1x + v1y*v1y + v1z*v1z), 1e-6f)
                val mag2 = max(sqrt(v2x*v2x + v2y*v2y + v2z*v2z), 1e-6f)
                out.add(acos((dot/(mag1*mag2)).coerceIn(-1f,1f)))
            }
        }
        return out
    }

    // static angles
    val lAngles = handAngles(left)
    val rAngles = handAngles(right)

    // elbow angles
    fun elbow(a: Triple<Float,Float,Float>, b: Triple<Float,Float,Float>, c: Triple<Float,Float,Float>): Float {
        val v1x = a.first - b.first; val v1y = a.second - b.second; val v1z = a.third - b.third
        val v2x = c.first - b.first; val v2y = c.second - b.second; val v2z = c.third - b.third
        val dot  = v1x*v2x + v1y*v2y + v1z*v2z
        val mag1 = max(sqrt(v1x*v1x + v1y*v1y + v1z*v1z), 1e-6f)
        val mag2 = max(sqrt(v2x*v2x + v2y*v2y + v2z*v2z), 1e-6f)
        return acos((dot/(mag1*mag2)).coerceIn(-1f,1f))
    }
    val eLeft  = elbow(pose4[0], pose4[2], left[0])    // l_sh, l_el, wrist
    val eRight = elbow(pose4[1], pose4[3], right[0])   // r_sh, r_el, wrist

    // inter-wrist dist
    val (lx,ly,lz) = left[0]; val (rx,ry,rz) = right[0]
    val distW = sqrt((lx-rx)*(lx-rx) + (ly-ry)*(ly-ry) + (lz-rz)*(lz-rz))

    return FloatArray(23).also { out ->
        var i = 0
        lAngles.forEach  { out[i++] = it }
        rAngles.forEach  { out[i++] = it }
        out[i++] = eLeft
        out[i++] = eRight
        out[i]   = distW
    }
}

/** Drop zero-frames, pad/downsample to exactly numFrames raw frames (FloatArray(138)). */
fun preprocessSequence(rawSeq: List<FloatArray>, numFrames: Int = 16): List<FloatArray> {
    val filtered = rawSeq.filter { frame -> frame.any{ kotlin.math.abs(it)>1e-6f } }
    return when {
        filtered.size >= numFrames -> (0 until numFrames).map { i ->
            val idx = ((i.toFloat()/(numFrames-1))*(filtered.size-1)).toInt()
            filtered[idx]
        }
        else -> List(numFrames - filtered.size){FloatArray(rawSeq[0].size)} + filtered
    }
}

/** Interpolate tiny zero-gaps per dimension in static features. */
fun interpolateMissing(static: List<FloatArray>): List<FloatArray> {
    val T = static.size; val D = static[0].size
    val arr = static.map{ it.copyOf() }.toTypedArray()
    for (d in 0 until D) {
        val col = FloatArray(T){ t-> arr[t][d] }
        val valid = (0 until T).filter{ kotlin.math.abs(col[it])>1e-6f }
        if (valid.isEmpty()) continue
        for (t in 0 until T) {
            if (kotlin.math.abs(col[t])<1e-6f) {
                val lower = valid.lastOrNull{it<t} ?: valid.first()
                val upper = valid.firstOrNull{it>t} ?: valid.last()
                val α = if (upper==lower) 0f else (t-lower)/(upper-lower).toFloat()
                arr[t][d] = col[lower]*(1-α) + col[upper]*α
            }
        }
    }
    return arr.toList()
}

/**
 * Builds a fixed-length dynamic sequence [numFrames x 69] from raw frames.
 * For model input, wrap as arrayOf(thisResult) for shape [1, numFrames, 69].
 */
fun computeDynamicFeatures(rawSeq: List<FloatArray>, numFrames: Int = 16): Array<FloatArray> {
    val seq = preprocessSequence(rawSeq, numFrames)
    val static = seq.map { computeFrameFeatures(it) }
    val interp = interpolateMissing(static)
    val vel = interp.mapIndexed { i, cur ->
        val prev = interp.getOrElse(i-1){cur}
        FloatArray(23){ j-> cur[j] - prev[j] }
    }
    val acc = vel.mapIndexed { i, cur ->
        val prev = vel.getOrElse(i-1){cur}
        FloatArray(23){ j-> cur[j] - prev[j] }
    }
    return Array(numFrames){ t ->
        FloatArray(69).also { out ->
            var i = 0
            interp[t].forEach{ out[i++]=it }
            vel   [t].forEach{ out[i++]=it }
            acc   [t].forEach{ out[i++]=it }
        }
    }
}
