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
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer
import com.signify.app.di.SignifyViewModelFactory
import com.signify.app.translator.viewmodel.TranslatorViewModel

@Composable
fun SignToTextPane(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    // 1) grab VM
    val factory = remember {
        SignifyViewModelFactory(
            container.lessonRepository,
            container.historyRepository,
            container.translatorRepository
        )
    }
    val vm: TranslatorViewModel = viewModel(factory = factory)

    // 2) camera permission
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
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

    // 3) UI state from VM
    val result by vm.translationResult.collectAsState(initial = "")

    // 4) UI
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // preview box
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
                    provider.bindToLifecycle( lifecycleOwner,
                        CameraSelector.DEFAULT_BACK_CAMERA, preview )
                }
                AndroidView({ previewView }, Modifier.fillMaxSize())
            } else {
                Text("Camera permission required", Modifier.align(Alignment.Center))
            }
        }

        Spacer(Modifier.height(12.dp))

        // start/stop button triggers a dummy detection + VM call
        var detecting by remember { mutableStateOf(false) }
        Button(onClick = {
            detecting = !detecting
            if (detecting) {
                // TODO: replace this stub with your actual sign‐landmark → code logic
                vm.translateSignCodes(listOf("SIGN-Hello"))
            }
        }) {
            Text(if (detecting) "Stop Detection" else "Start Detection")
        }

        Spacer(Modifier.height(16.dp))

        // result from VM
        Box(
            Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.TopStart
        ) {
            Text(
                text = if (result.isEmpty()) "Detected text will appear here" else result,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}
