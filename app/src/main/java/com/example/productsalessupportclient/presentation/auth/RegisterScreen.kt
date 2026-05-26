package com.example.productsalessupportclient.presentation.auth

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember

@Composable
fun RegisterScreen(
    viewModel: AuthViewModel,
    onBack: () -> Unit
) {
    val roles = listOf("purchaser", "manager", "storekeeper")
    var passwordVisible = remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Регистрация",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        OutlinedTextField(
            value = viewModel.registerFullName,
            onValueChange = { viewModel.registerFullName = it },
            label = { Text("ФИО") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.registerEmail,
            onValueChange = { viewModel.registerEmail = it },
            label = { Text("Email") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.registerPassword,
            onValueChange = { viewModel.registerPassword = it },
            label = { Text("Пароль") },
            singleLine = true,
            visualTransformation =
                if (passwordVisible.value)
                    androidx.compose.ui.text.input.VisualTransformation.None
                else
                    androidx.compose.ui.text.input.PasswordVisualTransformation(),
            trailingIcon = {
                val icon = if (passwordVisible.value) "видно" else "не видно"
                TextButton(onClick = { passwordVisible.value = !passwordVisible.value }) {
                    Text(icon)
                }
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = viewModel.registerPhone,
            onValueChange = { viewModel.registerPhone = it },
            label = { Text("Телефон") },
            singleLine = true
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text("Роль")

        Spacer(modifier = Modifier.height(8.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            roles.forEach { role ->
                FilterChip(
                    selected = viewModel.registerRole == role,
                    onClick = { viewModel.registerRole = role },
                    label = { Text(role) }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Button(
            onClick = { viewModel.register() },
            enabled = !viewModel.isLoading
        ) {
            Text("Отправить на подтверждение")
        }

        TextButton(onClick = onBack) {
            Text("Назад")
        }

        if (viewModel.isLoading) {
            Spacer(modifier = Modifier.height(12.dp))
            CircularProgressIndicator()
        }

        viewModel.registrationMessage?.let {
            Spacer(modifier = Modifier.height(12.dp))
            Text(it)
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