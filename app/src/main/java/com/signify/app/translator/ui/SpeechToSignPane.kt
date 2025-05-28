// File: app/src/main/java/com/signify/app/translator/ui/SpeechToSignPane.kt
package com.signify.app.translator.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer
import com.signify.app.translator.viewmodel.TranslatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SpeechToSignPane(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    val vm: TranslatorViewModel =
        viewModel(factory = container.viewModelFactory)
    val translation by vm.translationResult.collectAsState(initial = "")

    val context = LocalContext.current
    var hasAudioPerm by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.RECORD_AUDIO
            ) == PackageManager.PERMISSION_GRANTED
        )
    }
    val audioLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted -> hasAudioPerm = granted }
    LaunchedEffect(Unit) {
        if (!hasAudioPerm) audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
    }

    val speechRecognizer = remember { SpeechRecognizer.createSpeechRecognizer(context) }
    DisposableEffect(speechRecognizer) { onDispose { speechRecognizer.destroy() } }

    var isListening by remember { mutableStateOf(false) }
    LaunchedEffect(speechRecognizer) {
        speechRecognizer.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) = Unit
            override fun onBeginningOfSpeech() = Unit
            override fun onRmsChanged(rmsdB: Float) = Unit
            override fun onBufferReceived(buffer: ByteArray?) = Unit
            override fun onEndOfSpeech() { isListening = false }
            override fun onError(error: Int) { isListening = false }
            override fun onEvent(eventType: Int, params: Bundle?) = Unit
            override fun onPartialResults(partial: Bundle?) = Unit

            override fun onResults(results: Bundle?) {
                val text = results
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.joinToString(" ")
                    .orEmpty()
                vm.translateText(text)
                isListening = false
            }
        })
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        MaterialTheme.colorScheme.background,
                        MaterialTheme.colorScheme.surface
                    )
                )
            )
            .padding(16.dp)
    ) {
        Text("🎬 Sign Output", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        val codes = translation.split("\\s+".toRegex()).filter { it.isNotBlank() }
        if (codes.isEmpty()) {
            Text("Your sign sequence will appear here.",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(8.dp))
        } else {
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
            ) {
                items(codes) { code ->
                    Card(
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier
                            .width(80.dp)
                            .fillMaxHeight()
                    ) {
                        Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(code)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        val scale by animateFloatAsState(if (isListening) 1.2f else 1f)
        Box(
            Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp),
            contentAlignment = Alignment.Center
        ) {
            FloatingActionButton(
                onClick = {
                    if (!hasAudioPerm) {
                        audioLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    } else if (SpeechRecognizer.isRecognitionAvailable(context)) {
                        if (!isListening) {
                            val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                                putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                                    RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                                putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                            }
                            speechRecognizer.startListening(intent)
                            isListening = true
                        } else {
                            speechRecognizer.stopListening()
                            isListening = false
                        }
                    }
                },
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Filled.MicOff else Icons.Filled.Mic,
                    contentDescription = null
                )
            }
        }
    }
}
