// File: app/src/main/java/com/signify/app/auth/ui/RegisterScreen.kt
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
fun RegisterScreen(
    container: AppContainer,
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    val vm: AuthViewModel = viewModel(factory = container.viewModelFactory)
    val uiState by vm.uiState.collectAsState()

    var email         by remember { mutableStateOf("") }
    var password      by remember { mutableStateOf("") }
    var confirm       by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    LaunchedEffect(uiState) {
        if (uiState is AuthUiState.Success) onRegisterSuccess()
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
        Text("Sign Up", style = MaterialTheme.typography.displayLarge, color = Cream)
        Spacer(Modifier.height(8.dp))
        Text("Create your account",
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
            value = email, onValueChange = { email = it },
            label = { Text("Email", color = Cream) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Cream),
            modifier = Modifier.fillMaxWidth()
                .background(NavyVariant, shape = MaterialTheme.shapes.small)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                passwordError = (confirm.isNotEmpty() && confirm != it)
            },
            label = { Text("Password", color = Cream) },
            singleLine = true,
            visualTransformation = PasswordVisualTransformation(),
            textStyle = LocalTextStyle.current.copy(color = Cream),
            modifier = Modifier.fillMaxWidth()
                .background(NavyVariant, shape = MaterialTheme.shapes.small)
        )
        Spacer(Modifier.height(16.dp))
        OutlinedTextField(
            value = confirm,
            onValueChange = {
                confirm = it
                passwordError = (password.isNotEmpty() && it != password)
            },
            label = { Text("Confirm Password", color = Cream) },
            singleLine = true,
            isError = passwordError,
            visualTransformation = PasswordVisualTransformation(),
            textStyle = LocalTextStyle.current.copy(color = Cream),
            modifier = Modifier.fillMaxWidth()
                .background(NavyVariant, shape = MaterialTheme.shapes.small)
        )
        if (passwordError) {
            Text("Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.align(Alignment.Start)
                    .padding(start = 12.dp, top = 4.dp))
        }
        Spacer(Modifier.height(24.dp))
        Button(
            onClick = { vm.register(email, password) },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = OnGold),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier.fillMaxWidth().height(56.dp)
        ) {
            Text("SIGN UP", style = MaterialTheme.typography.labelLarge)
        }
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onBackToLogin) {
            Text("Already have an account? Log in", color = Cream)
        }
    }
}
