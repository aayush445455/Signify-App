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
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import com.google.mediapipe.tasks.vision.handlandmarker.*
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
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
    // 1) ViewModel & latest result
    val vm: TranslatorViewModel = viewModel(factory = container.viewModelFactory)
    val result by vm.translationResult.collectAsState(initial = "")

    // 2) Landmarks for overlay
    var landmarks by remember { mutableStateOf<List<NormalizedLandmark>>(emptyList()) }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 3) Camera permission
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

    // 5) ASL model + MediaPipe hand landmarker
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
                        landmarks = all[0]
                        // extract → predict
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

    // 6) CameraX preview + analyzer
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(hasCamPerm, detecting, useFrontCamera) {
        if (!hasCamPerm) return@LaunchedEffect
        val provider = cameraProviderFuture.get()
        provider.unbindAll()

        val previewUse = Preview.Builder()
            .build()
            .also { it.setSurfaceProvider(previewView.surfaceProvider) }

        if (detecting) {
            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build().also { ia ->
                    ia.setAnalyzer(executor) { img: ImageProxy ->
                        try {
                            val bmp = img.toBitmap()
                            val matrix = Matrix().apply {
                                postRotate(img.imageInfo.rotationDegrees.toFloat())
                                if (useFrontCamera)
                                    postScale(-1f, 1f, bmp.width / 2f, bmp.height / 2f)
                            }
                            val framed = Bitmap.createBitmap(
                                bmp, 0, 0, bmp.width, bmp.height, matrix, false
                            )
                            val mpImg = BitmapImageBuilder(framed).build()
                            handLandmarker.detectAsync(mpImg, img.imageInfo.timestamp)
                        } catch (e: Exception) {
                            Log.e("SignToTextPane", "analysis failed", e)
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
    Column(modifier = modifier.padding(16.dp)) {
        // camera + skeleton overlay
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
        ) {
            if (hasCamPerm) {
                AndroidView({ previewView }, Modifier.matchParentSize())
                Canvas(Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val bones = listOf(
                        0 to 1,1 to 2,2 to 3,3 to 4,
                        0 to 5,5 to 6,6 to 7,7 to 8,
                        0 to 9,9 to 10,10 to 11,11 to 12,
                        0 to 13,13 to 14,14 to 15,15 to 16,
                        0 to 17,17 to 18,18 to 19,19 to 20
                    )
                    bones.forEach { (a, b) ->
                        landmarks.getOrNull(a)?.let { p1 ->
                            landmarks.getOrNull(b)?.let { p2 ->
                                drawLine(
                                    Color.Green,
                                    Offset(p1.x()*w, p1.y()*h),
                                    Offset(p2.x()*w, p2.y()*h),
                                    strokeWidth = 4f
                                )
                            }
                        }
                    }
                    landmarks.forEach {
                        drawCircle(Color.Green, radius = 6f,
                            center = Offset(it.x()*w, it.y()*h))
                    }
                }
            } else {
                Text("Camera permission required",
                    Modifier.align(Alignment.Center))
            }
        }

        Spacer(Modifier.height(12.dp))

        // controls
        Row {
            Button(onClick = { detecting = !detecting }) {
                Text(if (detecting) "Stop" else "Start")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { useFrontCamera = !useFrontCamera }) {
                Text(if (useFrontCamera) "Back Cam" else "Front Cam")
            }
        }

        Spacer(Modifier.height(16.dp))

        // big single‐letter result styled to match your theme
        Box(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = result.ifEmpty { "Result" },
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 56.sp
                )
            )
        }
    }
}
