// File: app/src/main/java/com/signify/app/translator/TranslatorScreen.kt
package com.signify.app.translator

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier

@OptIn(ExperimentalMaterial3Api::class)

@Composable
fun TranslatorScreen() {
    val modes = listOf(
        Mode.SignToText   to "Sign → Text",
        Mode.SpeechToSign to "Speech → Sign",
        Mode.TextToSign   to "Text → Sign"
    )
    var selectedTab by remember { mutableStateOf(0) }

    Column(Modifier.fillMaxSize()) {
        // 1) TabRow at the top
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

        // 2) The pane container: take *only* the leftover space
        Box(
            Modifier
                .weight(1f)      // <-- this is key
                .fillMaxWidth()
        ) {
            when (modes[selectedTab].first) {
                Mode.SignToText   -> SignToTextPane(Modifier.fillMaxSize())
                Mode.SpeechToSign -> SpeechToSignPane(Modifier.fillMaxSize())
                Mode.TextToSign   -> TextToSignPane(Modifier.fillMaxSize())
            }
        }
    }
}
