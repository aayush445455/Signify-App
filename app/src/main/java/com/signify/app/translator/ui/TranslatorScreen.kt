// File: app/src/main/java/com/signify/app/translator/ui/TranslatorScreen.kt
package com.signify.app.translator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer
import com.signify.app.di.SignifyViewModelFactory
import com.signify.app.translator.viewmodel.TranslatorViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(
    container: AppContainer
) {
    // 1) Create ViewModel via Factory
    val factory = remember {
        SignifyViewModelFactory(
            container.lessonRepository,
            container.historyRepository,
            container.translatorRepository
        )
    }
    val vm: TranslatorViewModel = viewModel(factory = factory)

    // 2) Collect result StateFlow
    val result by vm.translationResult.collectAsState(initial = "")

    // 3) Tab definitions
    val modes = listOf(
        Mode.SignToText   to "Sign → Text",
        Mode.SpeechToSign to "Speech → Sign",
        Mode.TextToSign   to "Text → Sign"
    )
    var selectedTab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        // Tab headers
        TabRow(
            selectedTabIndex = selectedTab,
            containerColor   = MaterialTheme.colorScheme.surface,
            contentColor     = MaterialTheme.colorScheme.onSurface
        ) {
            modes.forEachIndexed { index, (_, title) ->
                Tab(
                    selected = index == selectedTab,
                    onClick  = { selectedTab = index },
                    text     = { Text(title) }
                )
            }
        }

        // Pane container
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (modes[selectedTab].first) {
                Mode.SignToText ->
                    SignToTextPane(
                        container = container,
                        modifier = Modifier.fillMaxSize()
                    )
                Mode.SpeechToSign ->
                    SpeechToSignPane(
                        container = container,
                        modifier = Modifier.fillMaxSize()
                    )
                Mode.TextToSign ->
                    TextToSignPane(
                        container = container,
                        modifier = Modifier.fillMaxSize()
                    )
            }
        }

        // (Optional) Show last translation result
        if (result.isNotEmpty()) {
            Text(
                text = result,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            )
        }
    }
}
