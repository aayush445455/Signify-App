package com.signify.app.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.signify.app.ui.theme.Cream
import com.signify.app.ui.theme.Navy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(

) {
    var darkMode by remember { mutableStateOf(false) }
    var ttsSpeed by remember { mutableStateOf(1f) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings", color = Cream) },
                navigationIcon = {

                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Navy,
                    titleContentColor = Cream
                )
            )
        },
        containerColor = Navy
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Navy)
                .padding(padding)
                .padding(24.dp)
        ) {
            // Dark Mode toggle
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("Dark Mode", color = Cream, style = MaterialTheme.typography.bodyLarge)
                Switch(
                    checked = darkMode,
                    onCheckedChange = { darkMode = it },
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary
                    )
                )
            }
            Spacer(Modifier.height(24.dp))

            // TTS Speed slider
            Text("Speech Speed: ${"%.1fx".format(ttsSpeed)}", color = Cream)
            Slider(
                value = ttsSpeed,
                onValueChange = { ttsSpeed = it },
                valueRange = 0.5f..2f,
                steps = 3,
                modifier = Modifier.fillMaxWidth(),
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary
                )
            )
        }
    }
}
