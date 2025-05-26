// File: app/src/main/java/com/signify/app/history/ui/HistoryScreen.kt
package com.signify.app.history.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer
import com.signify.app.di.SignifyViewModelFactory
import com.signify.app.history.viewmodel.HistoryViewModel
import com.signify.app.ui.theme.Cream
import com.signify.app.ui.theme.Navy
import com.signify.app.ui.theme.NavyVariant
import java.text.SimpleDateFormat
import java.util.*

data class HistoryEntryUI(
    val input: String,
    val output: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(container: AppContainer) {
    // 1) ViewModel
    val factory = remember {
        SignifyViewModelFactory(
            container.lessonRepository,
            container.historyRepository,
            container.translatorRepository
        )
    }
    val vm: HistoryViewModel = viewModel(factory = factory)

    // 2) Collect and map
    val domainHistory by vm.history.collectAsState(initial = emptyList())
    val formatter = remember { SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()) }
    val entries = domainHistory.map { h ->
        HistoryEntryUI(
            input     = h.inputText,
            output    = h.outputText,
            timestamp = formatter.format(Date(h.timestamp))
        )
    }

    var filter by remember { mutableStateOf("") }
    val filtered = entries.filter {
        it.input.contains(filter, ignoreCase = true) ||
                it.output.contains(filter, ignoreCase = true)
    }

    // 3) UI
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", color = Cream) },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Navy)
            )
        },
        containerColor = Navy
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .background(Navy)
                .padding(padding)
                .padding(16.dp)
        ) {
            OutlinedTextField(
                value = filter,
                onValueChange = { filter = it },
                label = { Text("Search history", color = Cream) },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .background(NavyVariant, shape = MaterialTheme.shapes.small),
                textStyle = LocalTextStyle.current.copy(color = Cream)
            )
            Spacer(Modifier.height(16.dp))

            if (filtered.isEmpty()) {
                Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No entries found", color = Cream)
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(filtered) { entry ->
                        Card(
                            Modifier
                                .fillMaxWidth()
                                .clickable { /* replay entry? */ },
                            colors = CardDefaults.cardColors(containerColor = NavyVariant)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = entry.timestamp,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                                    color = Cream.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text("In: ${entry.input}", style = MaterialTheme.typography.bodyLarge, color = Cream)
                                Spacer(Modifier.height(2.dp))
                                Text("Out: ${entry.output}", style = MaterialTheme.typography.bodyLarge, color = Cream)
                            }
                        }
                    }
                }
            }
        }
    }
}
