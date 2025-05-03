package com.signify.app.translator

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat

@Composable
fun SignToTextPane(
    modifier: Modifier = Modifier

) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1) Camera permission
    var hasCamPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val camLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamPerm = granted }

    LaunchedEffect(Unit) {
        if (!hasCamPerm) camLauncher.launch(Manifest.permission.CAMERA)
    }

    // 2) Stub analyzer
    var analyzing by remember { mutableStateOf(false) }
    var recognizedText by remember { mutableStateOf("Detected text will appear here") }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }
    val analyzer = remember {
        ImageAnalysis.Analyzer { image: ImageProxy ->
            if (analyzing) {
                recognizedText = "👋 Hello" // TODO: replace with ML inference
            }
            image.close()
        }
    }

    LaunchedEffect(hasCamPerm, analyzing) {
        if (!hasCamPerm) return@LaunchedEffect
        val provider = cameraProviderFuture.get()
        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageAnalysis = ImageAnalysis.Builder()
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
            .build()
            .also { it.setAnalyzer(ContextCompat.getMainExecutor(context), analyzer) }
        val selector = CameraSelector.DEFAULT_BACK_CAMERA
        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, selector, preview, imageAnalysis)
    }

    // 3) UI
    Column(
        modifier = modifier
            .padding(16.dp)
    ) {

        Spacer(Modifier.height(8.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
        ) {
            if (hasCamPerm) {
                AndroidView({ previewView }, Modifier.fillMaxSize())
            } else {
                Text("Camera permission required", Modifier.align(Alignment.Center))
            }
        }
        Spacer(Modifier.height(12.dp))

        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            Button(onClick = { analyzing = !analyzing }) {
                Text(if (analyzing) "Stop Detection" else "Start Detection")
            }
        }
        Spacer(Modifier.height(16.dp))

        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(8.dp),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = recognizedText,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
