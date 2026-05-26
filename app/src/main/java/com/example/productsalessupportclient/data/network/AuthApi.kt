package com.example.productsalessupportclient.data.network

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private const val BASE_URL = "http://10.0.2.2:8080/"

class AuthApi {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun login(email: String, password: String): LoginResponse {
        return client.post("${BASE_URL}/login") {
            contentType(ContentType.Application.Json)
            setBody(LoginRequest(email, password))
        }.body()
    }

    suspend fun me(token: String): MeResponse {
        return client.get("${BASE_URL}/me") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun register(request: RegisterRequest): RegistrationPendingResponse {
        return client.post("${BASE_URL}/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}