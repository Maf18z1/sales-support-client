package com.example.productsalessupportclient.data.network

import com.example.productsalesupportclient.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ReportApi {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getCategoryHistory(
        token: String,
        category: String
    ): List<CategoryHistoryPointResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/reports/category-history") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("category", category)
        }.body()
    }

    suspend fun getCategories(token: String): List<AssortmentWithStockResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/assortment") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}