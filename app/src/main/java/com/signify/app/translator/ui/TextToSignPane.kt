// File: app/src/main/java/com/signify/app/translator/ui/TextToSignPane.kt
package com.signify.app.translator.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer
import com.signify.app.di.SignifyViewModelFactory
import com.signify.app.translator.viewmodel.TranslatorViewModel

@Composable
fun TextToSignPane(
    container: AppContainer,
    modifier: Modifier = Modifier
) {
    // 1) VM
    val factory = remember {
        SignifyViewModelFactory(
            container.lessonRepository,
            container.historyRepository,
            container.translatorRepository
        )
    }
    val vm: TranslatorViewModel = viewModel(factory = factory)

    // 2) Local input
    var inputText by remember { mutableStateOf("") }

    // 3) Collect VM result
    val result by vm.translationResult.collectAsState(initial = "")

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
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
                // converts text→sign codes
                vm.translateText(inputText)
            },
            modifier = Modifier.align(Alignment.End)
        ) {
            Icon(Icons.Filled.PlayArrow, contentDescription = "Generate Sign")
            Spacer(Modifier.width(4.dp))
            Text("Generate")
        }

        Spacer(Modifier.height(16.dp))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(12.dp)
        ) {
            if (result.isEmpty()) {
                Text("Your sign sequence will appear here.")
            } else {
                result.split(" ").forEach { code ->
                    Text(code, style = MaterialTheme.typography.bodyLarge)
                    Spacer(Modifier.height(8.dp))
                }
            }
        }
    }
}
