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
import com.google.mediapipe.tasks.vision.core.RunningMode
import com.google.mediapipe.tasks.vision.handlandmarker.*
import com.google.mediapipe.tasks.vision.handlandmarker.HandLandmarker.HandLandmarkerOptions
import com.google.mediapipe.tasks.vision.holisticlandmarker.HolisticLandmarker
import com.google.mediapipe.tasks.vision.holisticlandmarker.HolisticLandmarkerResult
import com.signify.app.di.AppContainer
import com.signify.app.translator.asl.ASLInterpreter
import com.signify.app.translator.viewmodel.TranslatorViewModel
import com.signify.app.translator.asl.*
import com.signify.app.utils.toBitmap
import java.util.concurrent.Executors

@Composable
fun SignToTextPane(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val vm: TranslatorViewModel = viewModel(factory = container.viewModelFactory)
    val result by vm.translationResult.collectAsState(initial = "")

    // For static overlay
    var staticHandLandmarks by remember { mutableStateOf<List<NormalizedLandmark>>(emptyList()) }
    // For dynamic overlay (left hand, right hand, pose)
    var leftHandLandmarks by remember { mutableStateOf<List<NormalizedLandmark>>(emptyList()) }
    var rightHandLandmarks by remember { mutableStateOf<List<NormalizedLandmark>>(emptyList()) }
    var poseOverlayLandmarks by remember { mutableStateOf<List<NormalizedLandmark>>(emptyList()) }
    // For instant dynamic prediction
    var dynamicResult by remember { mutableStateOf("") }

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

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

    var detecting by remember { mutableStateOf(false) }
    var useFrontCamera by remember { mutableStateOf(false) }
    var useDynamicModel by remember { mutableStateOf(false) }
    val cameraSelector = if (useFrontCamera)
        CameraSelector.DEFAULT_FRONT_CAMERA
    else
        CameraSelector.DEFAULT_BACK_CAMERA

    val staticInterpreter = remember { ASLInterpreter(context) }
    val staticLabels = (0..9).map { it.toString() } + ('A'..'Z').map { it.toString() }
    val dynamicInterpreter = remember { DynamicASLInterpreter(context) }

    // ------ 1. HandLandmarker (STATIC) ------
    val handLandmarker = remember {
        HandLandmarker.createFromOptions(
            context,
            HandLandmarkerOptions.builder()
                .setBaseOptions(
                    com.google.mediapipe.tasks.core.BaseOptions.builder()
                        .setModelAssetPath("hand_landmarker.task")
                        .build()
                )
                .setNumHands(2)
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { res: HandLandmarkerResult, _ ->
                    if (!useDynamicModel) {
                        val all = res.landmarks()
                        if (all.isNotEmpty()) {
                            staticHandLandmarks = all[0]
                            val features = FeatureExtractor.extractFeatures(all[0])
                            val idx = staticInterpreter.predict(features)
                            val letter = staticLabels.getOrNull(idx) ?: "?"
                            vm.translateSignCodes(listOf(letter))
                        } else {
                            staticHandLandmarks = emptyList()
                        }
                    }
                }
                .setErrorListener { it.printStackTrace() }
                .build()
        )
    }

    // ------ 2. HolisticLandmarker (DYNAMIC) ------
    val holisticLandmarker = remember {
        HolisticLandmarker.createFromOptions(
            context,
            HolisticLandmarker.HolisticLandmarkerOptions.builder()
                .setBaseOptions(
                    com.google.mediapipe.tasks.core.BaseOptions.builder()
                        .setModelAssetPath("holistic_landmarker.task")
                        .build()
                )
                .setRunningMode(RunningMode.LIVE_STREAM)
                .setResultListener { res: HolisticLandmarkerResult, _ ->
                    if (useDynamicModel) {
                        val leftHand = res.leftHandLandmarks()
                        val rightHand = res.rightHandLandmarks()
                        val poseLandmarks = res.poseLandmarks()

                        // For overlays:
                        leftHandLandmarks = leftHand ?: emptyList()
                        rightHandLandmarks = rightHand ?: emptyList()
                        poseOverlayLandmarks = poseLandmarks ?: emptyList()

                        // For dynamic model: only commit if valid
                        val pose4 = listOf(
                            poseLandmarks.getOrNull(11),
                            poseLandmarks.getOrNull(12),
                            poseLandmarks.getOrNull(13),
                            poseLandmarks.getOrNull(14)
                        ).filterNotNull()
                        if (leftHand != null && rightHand != null && pose4.size == 4) {
                            val frame = flattenHolisticLandmarks(leftHand, rightHand, pose4)
                            dynamicInterpreter.processFrameRaw(frame) { label ->
                                if (label.isNotBlank() && label != "?") {
                                    if (label != dynamicResult) {
                                        dynamicResult = label
                                    }
                                }
                                vm.translateSignCodes(listOf(label))
                            }
                        }
                    }
                }
                .setErrorListener { it.printStackTrace() }
                .build()
        )
    }

    // ------ 3. CameraX setup ------
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val executor = remember { Executors.newSingleThreadExecutor() }

    LaunchedEffect(hasCamPerm, detecting, useFrontCamera, useDynamicModel) {
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
                            if (useDynamicModel) {
                                holisticLandmarker.detectAsync(mpImg, img.imageInfo.timestamp)
                            } else {
                                handLandmarker.detectAsync(mpImg, img.imageInfo.timestamp)
                            }
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

    // ------ 4. UI ------
    Column(modifier = modifier.padding(16.dp)) {
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

                    if (useDynamicModel) {
                        // ---- Hands (draw just like before) ----
                        val bones = listOf(
                            0 to 1,1 to 2,2 to 3,3 to 4,
                            0 to 5,5 to 6,6 to 7,7 to 8,
                            0 to 9,9 to 10,10 to 11,11 to 12,
                            0 to 13,13 to 14,14 to 15,15 to 16,
                            0 to 17,17 to 18,18 to 19,19 to 20
                        )
                        listOf(leftHandLandmarks, rightHandLandmarks).forEach { hand ->
                            bones.forEach { (a, b) ->
                                hand.getOrNull(a)?.let { p1 ->
                                    hand.getOrNull(b)?.let { p2 ->
                                        drawLine(
                                            Color.Green,
                                            Offset(p1.x() * w, p1.y() * h),
                                            Offset(p2.x() * w, p2.y() * h),
                                            strokeWidth = 4f
                                        )
                                    }
                                }
                            }
                            hand.forEach {
                                drawCircle(Color.Green, radius = 6f,
                                    center = Offset(it.x() * w, it.y() * h))
                            }
                        }
                        // Pose indices: 11 = left_shoulder, 12 = right_shoulder, 13 = left_elbow, 14 = right_elbow
                        val lShoulder = poseOverlayLandmarks.getOrNull(11)
                        val rShoulder = poseOverlayLandmarks.getOrNull(12)
                        val lElbow = poseOverlayLandmarks.getOrNull(13)
                        val rElbow = poseOverlayLandmarks.getOrNull(14)

// Draw shoulder to shoulder (top of skeleton)
                        if (lShoulder != null && rShoulder != null) {
                            drawLine(
                                Color.Blue,
                                Offset(lShoulder.x() * w, lShoulder.y() * h),
                                Offset(rShoulder.x() * w, rShoulder.y() * h),
                                strokeWidth = 5f
                            )
                        }

// Left arm (shoulder to elbow)
                        if (lShoulder != null && lElbow != null) {
                            drawLine(
                                Color.Blue,
                                Offset(lShoulder.x() * w, lShoulder.y() * h),
                                Offset(lElbow.x() * w, lElbow.y() * h),
                                strokeWidth = 5f
                            )
                        }

// Right arm (shoulder to elbow)
                        if (rShoulder != null && rElbow != null) {
                            drawLine(
                                Color.Blue,
                                Offset(rShoulder.x() * w, rShoulder.y() * h),
                                Offset(rElbow.x() * w, rElbow.y() * h),
                                strokeWidth = 5f
                            )
                        }

// Draw circles for joints
                        listOf(lShoulder, rShoulder, lElbow, rElbow).forEach { p ->
                            p?.let {
                                drawCircle(Color.Red, radius = 10f, center = Offset(it.x() * w, it.y() * h))
                            }
                        }

                    } else {
                        // Draw single hand (static)
                        val bones = listOf(
                            0 to 1,1 to 2,2 to 3,3 to 4,
                            0 to 5,5 to 6,6 to 7,7 to 8,
                            0 to 9,9 to 10,10 to 11,11 to 12,
                            0 to 13,13 to 14,14 to 15,15 to 16,
                            0 to 17,17 to 18,18 to 19,19 to 20
                        )
                        bones.forEach { (a, b) ->
                            staticHandLandmarks.getOrNull(a)?.let { p1 ->
                                staticHandLandmarks.getOrNull(b)?.let { p2 ->
                                    drawLine(
                                        Color.Green,
                                        Offset(p1.x() * w, p1.y() * h),
                                        Offset(p2.x() * w, p2.y() * h),
                                        strokeWidth = 4f
                                    )
                                }
                            }
                        }
                        staticHandLandmarks.forEach {
                            drawCircle(Color.Green, radius = 6f,
                                center = Offset(it.x() * w, it.y() * h))
                        }
                    }
                }
            } else {
                Text("Camera permission required",
                    Modifier.align(Alignment.Center))
            }
        }
        Spacer(Modifier.height(12.dp))
        Row {
            Button(onClick = { detecting = !detecting }) {
                Text(if (detecting) "Stop" else "Start")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { useFrontCamera = !useFrontCamera }) {
                Text(if (useFrontCamera) "Back Cam" else "Front Cam")
            }
            Spacer(Modifier.width(8.dp))
            Button(onClick = { useDynamicModel = !useDynamicModel }) {
                Text(if (useDynamicModel) "Dynamic" else "Static")
            }
        }
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .height(80.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (useDynamicModel) dynamicResult.ifEmpty { "Result" } else result.ifEmpty { "Result" },
                style = MaterialTheme.typography.displayLarge.copy(
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontSize = 56.sp
                )
            )
        }
    }
}
