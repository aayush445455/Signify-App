package com.signify.app.translator

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

@Composable
fun TextToSignPane(
    modifier: Modifier = Modifier

) {
    var inputText by remember { mutableStateOf("") }
    var signOutput by remember { mutableStateOf<List<String>>(emptyList()) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        Spacer(Modifier.height(8.dp))

        OutlinedTextField(
            value = inputText,
            onValueChange = { inputText = it },
            label = { Text("Enter text to convert") },
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(min = 56.dp)
        )
        Spacer(Modifier.height(12.dp))

        Button(
            onClick = {
                signOutput = inputText
                    .split(" ")
                    .map { "🔲 $it (sign)" }
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Generate Sign")
            Spacer(Modifier.width(4.dp))
            Text("Generate Sign")
        }
        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .background(Color(0xFF1A254B))
                .padding(12.dp)
        ) {
            if (signOutput.isEmpty()) {
                Text(
                    "Your sign sequence will appear here.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            } else {
                signOutput.forEach { placeholder ->
                    Text(text = placeholder, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
