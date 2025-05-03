// File: app/src/main/java/com/signify/app/history/HistoryScreen.kt
package com.signify.app.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.signify.app.ui.theme.Cream
import com.signify.app.ui.theme.Navy
import com.signify.app.ui.theme.NavyVariant

data class HistoryEntry(
    val input: String,
    val output: String,
    val timestamp: String
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(

    entries: List<HistoryEntry> = sampleHistory()
) {
    var filter by remember { mutableStateOf("") }
    val filtered = entries.filter {
        it.input.contains(filter, ignoreCase = true) ||
                it.output.contains(filter, ignoreCase = true)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("History", color = Cream) },
                navigationIcon = {
                },
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
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { /* maybe re-run this entry */ },
                            colors = CardDefaults.cardColors(containerColor = NavyVariant)
                        ) {
                            Column(Modifier.padding(12.dp)) {
                                Text(
                                    text = entry.timestamp,
                                    style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Light),
                                    color = Cream.copy(alpha = 0.7f)
                                )
                                Spacer(Modifier.height(4.dp))
                                Text(
                                    text = "In: ${entry.input}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Cream
                                )
                                Spacer(Modifier.height(2.dp))
                                Text(
                                    text = "Out: ${entry.output}",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = Cream
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Sample dummy data – replace with your Room/DB-backed list */
private fun sampleHistory() = listOf(
    HistoryEntry("Hello", "👋 Hello", "2025-04-28 10:15"),
    HistoryEntry("How are you?", "🤟😊", "2025-04-28 10:17"),
    HistoryEntry("Thank you", "🙏 Thank you", "2025-04-28 10:20"),
    HistoryEntry("Yes", "👍 Yes", "2025-04-28 10:25"),
    HistoryEntry("No", "👎 No", "2025-04-28 10:30")
)
