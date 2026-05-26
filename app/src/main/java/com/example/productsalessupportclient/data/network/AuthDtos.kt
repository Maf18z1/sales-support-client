package com.example.productsalessupportclient.data.network

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequest(
    val email: String,
    val password: String
)

@Serializable
data class LoginResponse(
    val token: String
)

@Serializable
data class RegisterRequest(
    val email: String,
    val password: String,
    val fullName: String? = null,
    val role: String = "client",
    val phone: String? = null
)

@Serializable
data class RegistrationPendingResponse(
    val message: String,
    val expiresAt: String
)

@Serializable
data class MeResponse(
    val userId: Long,
    val email: String,
    val fullName: String?,
    val phone: String?,
    val role: String
)