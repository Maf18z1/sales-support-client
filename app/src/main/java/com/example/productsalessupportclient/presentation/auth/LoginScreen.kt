package com.example.productsalessupportclient.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun LoginScreen(
    viewModel: AuthViewModel,
    onRegisterClick: () -> Unit,
    onLoginSuccess: (String) -> Unit
) {
    var passwordVisible = remember { mutableStateOf(false) }

    LaunchedEffect(viewModel.loggedInRole) {
        viewModel.loggedInRole?.let { role ->
            onLoginSuccess(role)
            viewModel.clearLoginResult()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Вход",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.loginEmail,
            onValueChange = { viewModel.loginEmail = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.loginPassword,
            onValueChange = { viewModel.loginPassword = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation =
                if (passwordVisible.value)
                    androidx.compose.ui.text.input.VisualTransformation.None
                else
                    androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                IconButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Icon(
                        imageVector = if (passwordVisible.value)
                            Icons.Filled.Visibility
                        else
                            Icons.Filled.VisibilityOff,
                        contentDescription = if (passwordVisible.value)
                            "Скрыть пароль"
                        else
                            "Показать пароль"
                    )
                }
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.login() },
            enabled = !viewModel.isLoading
        ) {
            Text("Войти")
        }

        TextButton(onClick = onRegisterClick) {
            Text("Регистрация")
        }

        if (viewModel.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        viewModel.error?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
        }
    }
}