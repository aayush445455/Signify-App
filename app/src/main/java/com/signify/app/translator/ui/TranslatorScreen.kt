// File: app/src/main/java/com/signify/app/translator/ui/TranslatorScreen.kt
package com.signify.app.translator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TranslatorScreen(container: AppContainer) {
    val modes = listOf(
        Mode.SignToText   to "Sign → Text",
        Mode.SpeechToSign to "Speech → Sign",
        Mode.TextToSign   to "Text → Sign"
    )
    var selectedTab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
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
        Box(
            Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            when (modes[selectedTab].first) {
                Mode.SignToText   -> SignToTextPane(container, Modifier.fillMaxSize())
                Mode.SpeechToSign -> SpeechToSignPane(container, Modifier.fillMaxSize())
                Mode.TextToSign   -> TextToSignPane(container, Modifier.fillMaxSize())
            }
        }
    }
}
