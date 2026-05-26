package com.example.productsalessupportclient.data.repository

import com.example.productsalessupportclient.data.network.AuthApi
import com.example.productsalessupportclient.data.network.MeResponse
import com.example.productsalessupportclient.data.network.RegisterRequest
import com.example.productsalessupportclient.data.network.RegistrationPendingResponse

data class AuthSession(
    val token: String,
    val profile: MeResponse
)

class AuthRepository(
    private val api: AuthApi
) {
    suspend fun login(email: String, password: String): AuthSession {
        val token = api.login(email, password).token
        val me = api.me(token)
        return AuthSession(token = token, profile = me)
    }

    suspend fun register(request: RegisterRequest): RegistrationPendingResponse {
        return api.register(request)
    }
}