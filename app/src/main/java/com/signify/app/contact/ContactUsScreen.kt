package com.signify.app.contact

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import com.signify.app.ui.theme.Cream
import com.signify.app.ui.theme.Navy

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactUsScreen() {
    val ctx = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Contact Us", color = Cream) },
                navigationIcon = {
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor    = Navy,
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
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("We’d love to hear from you!", color = Cream, style = MaterialTheme.typography.headlineSmall)
            ContactRow("Email", "help@signify.app") {
                val intent = Intent(Intent.ACTION_SENDTO, "mailto:help@signify.app".toUri())
                ctx.startActivity(intent)
            }
            ContactRow("Website", "https://signify.app") {
                val intent = Intent(Intent.ACTION_VIEW, "https://signify.app".toUri())
                ctx.startActivity(intent)
            }
            ContactRow("Phone", "+1-800-SIGNIFY") {
                val intent = Intent(Intent.ACTION_DIAL, "tel:+18007464439".toUri())
                ctx.startActivity(intent)
            }
        }
    }
}

@Composable
private fun ContactRow(label: String, detail: String, onClick: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp)
    ) {
        Text("$label:", color = Cream, modifier = Modifier.weight(1f))
        Text(detail, color = MaterialTheme.colorScheme.primary)
    }
}
