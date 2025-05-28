// File: app/src/main/java/com/signify/app/auth/ui/LoginScreen.kt
package com.signify.app.auth.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.signify.app.di.AppContainer
import com.signify.app.R
import com.signify.app.auth.viemodel.AuthUiState
import com.signify.app.auth.viemodel.AuthViewModel
import com.signify.app.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    container: AppContainer,
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val vm: AuthViewModel = viewModel(factory = container.viewModelFactory)
    val uiState by vm.uiState.collectAsState()

    var email    by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    // navigate on success
    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onLoginSuccess()
    }

    Column(
        Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement   = Arrangement.Top
    ) {
        Spacer(Modifier.height(48.dp))

        Icon(
            painter = painterResource(R.drawable.ic_signify_logo),
            contentDescription = null,
            tint = Cream,
            modifier = Modifier.size(160.dp).padding(bottom = 32.dp)
        )
        Text("Signify", style = MaterialTheme.typography.displayLarge, color = Cream)
        Spacer(Modifier.height(8.dp))
        Text("A Sign Language Interpreter App",
            style = MaterialTheme.typography.headlineSmall, color = Cream)

        Spacer(Modifier.height(32.dp))
        if (uiState is AuthUiState.Loading) {
            CircularProgressIndicator(color = Gold)
            Spacer(Modifier.height(16.dp))
        }
        if (uiState is AuthUiState.Error) {
            Text((uiState as AuthUiState.Error).message.orEmpty(),
                color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(16.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Cream) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Cream),
            modifier = Modifier.fillMaxWidth()
                .background(NavyVariant, shape = MaterialTheme.shapes.small)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password", color = Cream) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            textStyle = LocalTextStyle.current.copy(color = Cream),
            modifier = Modifier.fillMaxWidth()
                .background(NavyVariant, shape = MaterialTheme.shapes.small)
        )
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.login(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = OnGold),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("LOG IN", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNavigateToRegister) {
            Text("Don’t have an account? Sign Up", color = Cream)
        }
    }
}
