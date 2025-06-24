// File: app/src/main/java/com/signify/app/translator/ui/SignToTextPane.kt
package com.signify.app.translator.ui

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Matrix
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.mediapipe.framework.image.BitmapImageBuilder
import com.google.mediapipe.tasks.components.containers.NormalizedLandmark
import com.google.mediapipe.tasks.core.BaseOptions
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarkerResult
import com.signify.app.di.AppContainer
import com.signify.app.translator.asl.ASLInterpreter
import com.signify.app.translator.asl.FeatureExtractor
import com.signify.app.translator.viewmodel.TranslatorViewModel
import com.signify.app.utils.toBitmap
import java.util.concurrent.Executors

@Composable
fun SignToTextPane(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    // 1) ViewModel & last-recognized letter
    val vm: TranslatorViewModel = viewModel(factory = container.viewModelFactory)
    val result by vm.translationResult.collectAsState("")

    // 2) Hold the latest 21-point hand landmarks
    var landmarks by remember { mutableStateOf<List<NormalizedLandmark>>(emptyList()) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 3) Permissions
    var hasCamPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val permLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamPerm = granted }
    LaunchedEffect(Unit) {
        if (!hasCamPerm) permLauncher.launch(Manifest.permission.CAMERA)
    }

    // 4) Toggles
    var detecting by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    val cameraSelector = if (useFrontCamera)
        CameraSelector.DEFAULT_FRONT_CAMERA
    else
        CameraSelector.DEFAULT_BACK_CAMERA

    // 5) ML & MediaPipe
    val aslInterpreter = remember { ASLInterpreter(context) }
    val handLandmarker = remember {
        HandLandmarker.createFromOptions(
            context,
            HandLandmarkerOptions.builder()
                .setBaseOptions(
                    BaseOptions.builder()
                        .setModelAssetPath("hand_landmarker.task")
                        .build()
                )
                .setNumHands(1)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { res: HandLandmarkerResult, _ ->
                    val all = res.landmarks()
                    if (all.isNotEmpty()) {
                        // update overlay
                        landmarks = all[0]
                        // do your existing predict
                        val features = FeatureExtractor.extractFeatures(landmarks)
                        val idx = aslInterpreter.predict(features)
                        val labels = (0..9).map { it.toString() } +
                                ('A'..'Z').map { it.toString() }
                        val letter = labels.getOrNull(idx) ?: "?"
                        vm.translateSignCodes(listOf(letter))
                    } else {
                        landmarks = emptyList()
                    }
                }
                .setErrorListener { it.printStackTrace() }
                .build()
        )
    }

    // 6) CameraX setup
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(hasCamPerm, detecting, useFrontCamera) {
        if (!hasCamPerm) return@LaunchedEffect
        val provider = cameraProviderFuture.get()
        provider.unbindAll()

        val previewUse = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        if (detecting) {
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { ia ->
                    ia.setAnalyzer(executor) { img: ImageProxy ->
                        try {
                            // rotate & mirror for front camera
                            val bmp = img.toBitmap()
                            val matrix = Matrix().apply {
                                postRotate(img.imageInfo.rotationDegrees.toFloat())
                                if (useFrontCamera)
                                    postScale(-1f, 1f, bmp.width/2f, bmp.height/2f)
                            }
                            val framed = Bitmap.createBitmap(
                                bmp, 0, 0, bmp.width, bmp.height, matrix, false
                            )
                            val mpImg = BitmapImageBuilder(framed).build()
                            handLandmarker.detectAsync(mpImg, img.imageInfo.timestamp)
                        } catch (e: Exception) {
                            Log.e("SignToTextPane", "analyze failed", e)
                        } finally {
                            img.close()
                        }
                    }
                }
            provider.bindToLifecycle(
                lifecycleOwner, cameraSelector, previewUse, analysis
            )
        } else {
            provider.bindToLifecycle(
                lifecycleOwner, cameraSelector, previewUse
            )
        }
    }

    // 7) UI
    Column(modifier.padding(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f/4f)
        ) {
            if (hasCamPerm) {
                AndroidView({ previewView }, Modifier.matchParentSize())
                // -- overlay skeleton --
                Canvas(Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height

                    // MediaPipe hand skeleton edges
                    val bones = listOf(
                        0 to 1, 1 to 2, 2 to 3, 3 to 4,
                        0 to 5, 5 to 6, 6 to 7, 7 to 8,
                        0 to 9, 9 to 10,10 to 11,11 to 12,
                        0 to 13,13 to 14,14 to 15,15 to 16,
                        0 to 17,17 to 18,18 to 19,19 to 20
                    )

                    // draw bones
                    for ((a,b) in bones) {
                        val p1 = landmarks.getOrNull(a)
                        val p2 = landmarks.getOrNull(b)
                        if (p1!=null && p2!=null) {
                            drawLine(
                                Color.Green,
                                Offset(p1.x()*w, p1.y()*h),
                                Offset(p2.x()*w, p2.y()*h),
                                strokeWidth = 4f
                            )
                        }
                    }
                    // draw joints
                    for (lm in landmarks) {
                        drawCircle(
                            Color.Green,
                            radius = 6f,
                            center = Offset(lm.x()*w, lm.y()*h)
                        )
                    }
                }
            } else {
                Text("Camera permission required", Modifier.align(Alignment.Center))
            }
        }

        Spacer(Modifier.height(12.dp))

        Row {
            Button(onClick = { detecting = !detecting }) {
                Text(if (detecting) "Stop Detection" else "Start Detection")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { useFrontCamera = !useFrontCamera }) {
                Text(if (useFrontCamera) "Use Back Cam" else "Use Front Cam")
            }
        }

        Spacer(Modifier.height(16.dp))

        // Big centered letter
        Box(
            Modifier
                .fillMaxWidth()
                .height(100.dp),
            contentAlignment = Alignment.Center
        ) {
            if (result.isNotEmpty()) {
                Text(
                    text = result,
                    fontSize = 56.sp,
                    color = Color.White,
                    modifier = Modifier
                        .background(Color(0x88000000), RoundedCornerShape(8.dp))
                        .padding(horizontal = 32.dp, vertical = 16.dp)
                )
            } else {
                Text("Detected text will appear here", style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
