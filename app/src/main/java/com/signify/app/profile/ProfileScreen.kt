package com.signify.app.profile

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.signify.app.R
import com.signify.app.ui.theme.Cream
import com.signify.app.ui.theme.Gold
import com.signify.app.ui.theme.Navy
import com.signify.app.ui.theme.NavyVariant

@Composable
fun ProfileScreen(
    userName: String = "John Doe",
    userEmail: String = "john.doe@example.com",
    onNavigate: (String) -> Unit,
    onLogout: () -> Unit
) {
    Column(
        Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Avatar
        Box(
            Modifier
                .size(96.dp)
                .background(NavyVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(R.drawable.ic_signify_logo),
                contentDescription = "User Avatar",
                modifier = Modifier.size(64.dp)
            )
        }
        Spacer(Modifier.height(16.dp))
        Text(userName, style = MaterialTheme.typography.headlineSmall, color = Cream)
        Spacer(Modifier.height(4.dp))
        Text(userEmail, style = MaterialTheme.typography.bodyMedium, color = Cream.copy(alpha = 0.7f))
        Spacer(Modifier.height(32.dp))

        // Quick Links
        val items = listOf(
            "History" to { onNavigate("history") },
            "Lessons" to { onNavigate("lessons") },
            "Settings" to { onNavigate("settings") }
        )
        items.forEach { (label, action) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable(onClick = action),
                colors = CardDefaults.cardColors(containerColor = NavyVariant)
            ) {
                Text(
                    text = label,
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = Cream
                )
            }
        }

        Spacer(Modifier.weight(1f))

        // Logout
        Button(
            onClick = onLogout,
            colors = ButtonDefaults.buttonColors(
                containerColor = Gold,
                contentColor   = Navy
            ),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = MaterialTheme.shapes.large
        ) {
            Text("LOG OUT", style = MaterialTheme.typography.labelLarge)
        }
    }
}
