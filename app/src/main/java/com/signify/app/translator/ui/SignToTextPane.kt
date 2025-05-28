// File: app/src/main/java/com/signify/app/translator/ui/SignToTextPane.kt
package com.signify.app.translator.ui

import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer
import com.signify.app.translator.viewmodel.TranslatorViewModel

@Composable
fun SignToTextPane(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    // pull your shared factory
    val vm: TranslatorViewModel = viewModel(factory = container.viewModelFactory)
    val result by vm.translationResult.collectAsState(initial = "")

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCamPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED
        )
    }
    val camLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasCamPerm = granted }
    LaunchedEffect(Unit) {
        if (!hasCamPerm) camLauncher.launch(Manifest.permission.CAMERA)
    }

    Column(modifier = modifier.padding(16.dp)) {
        Box(
            Modifier
                .fillMaxWidth()
                .aspectRatio(3f / 4f)
        ) {
            if (hasCamPerm) {
                val previewView = remember { PreviewView(context) }
                val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
                LaunchedEffect(hasCamPerm) {
                    val provider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.setSurfaceProvider(previewView.surfaceProvider)
                    }
                    provider.unbindAll()
                    provider.bindToLifecycle(
                        lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA,
                        preview
                    )
                }
                AndroidView({ previewView }, Modifier.fillMaxSize())
            } else {
                Text("Camera permission required", Modifier.align(Alignment.Center))
            }
        }
        Spacer(Modifier.height(12.dp))
        var detecting by remember { mutableStateOf(false) }
        Button(onClick = {
            detecting = !detecting
            if (detecting) {
                vm.translateSignCodes(listOf("SIGN-HELLO"))
            }
        }) {
            Text(if (detecting) "Stop Detection" else "Start Detection")
        }
        Spacer(Modifier.height(16.dp))
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopStart
        ) {
            Text(if (result.isEmpty()) "Detected text will appear here" else result)
        }
    }
}
