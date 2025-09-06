package com.signify.app.utils

import android.graphics.*
import androidx.camera.core.ImageProxy
import java.io.ByteArrayOutputStream

/** Convert this ImageProxy (YUV_420_888) into a correctly-oriented RGB Bitmap */
fun ImageProxy.toBitmap(): Bitmap {
    // 1) Convert YUV planes to NV21 byte array
    val nv21 = yuv420888ToNv21(this)

    // 2) Wrap in YuvImage and compress to JPEG (full quality)
    val yuvImage = YuvImage(nv21, ImageFormat.NV21, width, height, null)
    val out = ByteArrayOutputStream()
    yuvImage.compressToJpeg(Rect(0, 0, width, height), 100, out)
    val jpegBytes = out.toByteArray()

    // 3) Decode the JPEG bytes into a Bitmap
    val bitmap = BitmapFactory.decodeByteArray(jpegBytes, 0, jpegBytes.size)

    // 4) Rotate the bitmap to match the sensor orientation
    val rotation = imageInfo.rotationDegrees.toFloat()
    return if (rotation != 0f) {
        val matrix = Matrix().apply { postRotate(rotation) }
        Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    } else {
        bitmap
    }
}

/** Helper: pack the three ImageProxy planes into one NV21 byte array */
private fun yuv420888ToNv21(image: ImageProxy): ByteArray {
    val ySize = image.planes[0].buffer.remaining()
    val uSize = image.planes[1].buffer.remaining()
    val vSize = image.planes[2].buffer.remaining()

    // NV21 = YYYYYYYY VUVU
    val nv21 = ByteArray(ySize + uSize + vSize)
    image.planes[0].buffer.get(nv21, 0, ySize)

    // Interleave V and U
    val chromaRowStride = image.planes[1].rowStride
    val chromaPixelStride = image.planes[1].pixelStride
    var uvPos = ySize
    for (row in 0 until image.height / 2) {
        for (col in 0 until image.width / 2) {
            val uIndex = row * chromaRowStride + col * chromaPixelStride
            val vIndex = row * image.planes[2].rowStride + col * image.planes[2].pixelStride
            nv21[uvPos++] = image.planes[2].buffer.get(vIndex)  // V
            nv21[uvPos++] = image.planes[1].buffer.get(uIndex)  // U
        }
    }
    return nv21
}
