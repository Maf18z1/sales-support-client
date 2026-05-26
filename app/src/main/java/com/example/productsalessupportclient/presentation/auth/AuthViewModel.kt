package com.example.productsalessupportclient.presentation.auth

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.RegisterRequest
import com.example.productsalessupportclient.data.repository.AuthRepository
import com.example.productsalessupportclient.data.repository.AuthSession
import kotlinx.coroutines.launch

class AuthViewModel(
    private val repository: AuthRepository
) : ViewModel() {

    var loginEmail by mutableStateOf("")
    var loginPassword by mutableStateOf("")

    var registerEmail by mutableStateOf("")
    var registerPassword by mutableStateOf("")
    var registerFullName by mutableStateOf("")
    var registerPhone by mutableStateOf("")
    var registerRole by mutableStateOf("client")

    var isLoading by mutableStateOf(false)
    var error by mutableStateOf<String?>(null)
    var registrationMessage by mutableStateOf<String?>(null)
    var currentSession by mutableStateOf<AuthSession?>(null)
    var loggedInRole by mutableStateOf<String?>(null)
        private set

    fun clearLoginResult() {
        loggedInRole = null
    }

    fun clearMessages() {
        error = null
        registrationMessage = null
    }

    fun login() {
        viewModelScope.launch {
            isLoading = true
            error = null
            try {
                val session = repository.login(loginEmail.trim(), loginPassword)
                currentSession = session
                loggedInRole = session.profile.role.lowercase()
            } catch (e: Exception) {
                error = e.message ?: "Login failed"
            } finally {
                isLoading = false
            }
        }
    }

    fun logout() {
        currentSession = null
        loggedInRole = null
        loginEmail = ""
        loginPassword = ""
        error = null
        registrationMessage = null
    }

    fun register() {
        viewModelScope.launch {
            isLoading = true
            error = null
            registrationMessage = null
            try {
                val response = repository.register(
                    RegisterRequest(
                        email = registerEmail.trim(),
                        password = registerPassword,
                        fullName = registerFullName.ifBlank { null },
                        role = registerRole,
                        phone = registerPhone.ifBlank { null }
                    )
                )

                registrationMessage = "${response.message}\nСрок подтверждения: ${response.expiresAt}"
            } catch (e: Exception) {
                error = e.message ?: "Registration failed"
            } finally {
                isLoading = false
            }
        }
    }
}