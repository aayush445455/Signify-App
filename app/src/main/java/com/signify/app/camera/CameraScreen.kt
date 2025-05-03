// File: app/src/main/java/com/signify/app/camera/CameraScreen.kt
package com.signify.app.camera

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
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
fun CameraScreen(
    onNavigateToSpeech:     () -> Unit = {},
    onNavigateToTextToSign: () -> Unit = {},
    onNavigateToSignToText: () -> Unit = {}
) {
    val context        = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1) Camera permission
    var hasCamPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val camLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamPerm = granted }
    LaunchedEffect(Unit) {
        if (!hasCamPerm) camLauncher.launch(Manifest.permission.CAMERA)
    }

    // 2) CameraX setup
    var lensFacing by remember { mutableStateOf(CameraSelector.LENS_FACING_BACK) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    val previewView = remember { PreviewView(context) }

    LaunchedEffect(hasCamPerm, lensFacing) {
        if (!hasCamPerm) return@LaunchedEffect

        val provider  = cameraProviderFuture.get()
        val preview   = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }
        val imageCap  = ImageCapture.Builder().build()
        val selector  = CameraSelector.Builder()
            .requireLensFacing(lensFacing)
            .build()

        provider.unbindAll()
        provider.bindToLifecycle(lifecycleOwner, selector, preview, imageCap)
    }

    // 3) UI
    Column(Modifier.fillMaxSize()) {
        // Preview box (3:4)
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

        Spacer(Modifier.weight(1f))

        // Capture button
        Box(Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            FloatingActionButton(onClick = { /* TODO: capture photo */ }) {
                Icon(Icons.Filled.CameraAlt, contentDescription = "Capture")
            }
        }

        // Switch camera button
        Row(
            Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            FloatingActionButton(onClick = {
                lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK)
                    CameraSelector.LENS_FACING_FRONT
                else
                    CameraSelector.LENS_FACING_BACK
            }) {
                Icon(Icons.Filled.Cached, contentDescription = "Switch Camera")
            }
        }
    }
}
