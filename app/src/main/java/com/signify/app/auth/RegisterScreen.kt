// File: app/src/main/java/com/signify/app/auth/RegisterScreen.kt
package com.signify.app.auth

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.signify.app.R
import com.signify.app.ui.theme.Cream
import com.signify.app.ui.theme.Gold
import com.signify.app.ui.theme.Navy
import com.signify.app.ui.theme.NavyVariant
import com.signify.app.ui.theme.OnGold

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onRegisterSuccess: () -> Unit,
    onBackToLogin: () -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirm by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf(false) }

    Column(
        Modifier
            .fillMaxSize()
            .background(Navy)
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(48.dp))

        Image(
            painter = painterResource(R.drawable.ic_signify_logo),
            contentDescription = "Signify Logo",
            modifier = Modifier
                .size(160.dp)
                .padding(bottom = 32.dp)
        )

        Text("Sign Up", style = MaterialTheme.typography.displayLarge, color = Cream)
        Spacer(Modifier.height(8.dp))
        Text("Create your account",
            style = MaterialTheme.typography.headlineSmall, color = Cream)

        Spacer(Modifier.height(32.dp))

        // Email
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email", color = Cream) },
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(color = Cream),
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyVariant, shape = MaterialTheme.shapes.small)
        )
        Spacer(Modifier.height(16.dp))

        // Password
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
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyVariant, shape = MaterialTheme.shapes.small)
        )
        Spacer(Modifier.height(16.dp))

        // Confirm
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
            modifier = Modifier
                .fillMaxWidth()
                .background(NavyVariant, shape = MaterialTheme.shapes.small)
        )
        if (passwordError) {
            Text("Passwords do not match",
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier
                    .align(Alignment.Start)
                    .padding(start = 12.dp, top = 4.dp))
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (!passwordError && email.isNotBlank() && password.isNotBlank()) {
                    onRegisterSuccess()
                }
            },
            colors = ButtonDefaults.buttonColors(containerColor = Gold, contentColor = OnGold),
            shape = MaterialTheme.shapes.large,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            Text("SIGN UP", style = MaterialTheme.typography.labelLarge)
        }

        Spacer(Modifier.height(12.dp))

        TextButton(onClick = onBackToLogin) {
            Text("Already have an account? Log in", color = Cream)
        }
    }
}
