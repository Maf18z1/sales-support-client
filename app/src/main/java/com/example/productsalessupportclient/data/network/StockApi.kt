package com.example.productsalessupportclient.data.network

import com.example.productsalesupportclient.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class StockApi {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getOverview(
        token: String,
        category: String? = null,
        minStock: Int? = null,
        maxStock: Int? = null,
        expiringDays: Int? = null
    ): List<StockOverviewResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/stock/overview") {
            header(HttpHeaders.Authorization, "Bearer $token")
            category?.takeIf { it.isNotBlank() }?.let { parameter("category", it) }
            minStock?.let { parameter("minStock", it) }
            maxStock?.let { parameter("maxStock", it) }
            expiringDays?.let { parameter("expiringDays", it) }
        }.body()
    }

    suspend fun getDetail(token: String, assortmentId: Long): StockDetailResponse {
        return client.get("${BuildConfig.API_BASE_URL}/stock/$assortmentId/detail") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun updateDetail(
        token: String,
        assortmentId: Long,
        request: StockDetailUpsertRequest
    ): StockDetailResponse {
        return client.put("${BuildConfig.API_BASE_URL}/stock/$assortmentId/detail") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun setStock(
        token: String,
        assortmentId: Long,
        quantity: Int
    ): Map<String, String> {
        return client.post("${BuildConfig.API_BASE_URL}/stock/$assortmentId/set") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(StockSetRequest(quantity))
        }.body()
    }

    suspend fun getBatches(
        token: String,
        assortmentId: Long
    ): List<StockBatchResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/batches") {
            header(HttpHeaders.Authorization, "Bearer $token")
            parameter("assortmentId", assortmentId)
        }.body()
    }

    suspend fun createBatch(
        token: String,
        request: StockBatchUpsertRequest
    ): StockBatchResponse {
        return client.post("${BuildConfig.API_BASE_URL}/batches") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun updateBatch(
        token: String,
        batchId: Long,
        request: StockBatchUpsertRequest
    ): StockBatchResponse {
        return client.put("${BuildConfig.API_BASE_URL}/batches/$batchId") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteBatch(
        token: String,
        batchId: Long
    ): Map<String, String> {
        return client.delete("${BuildConfig.API_BASE_URL}/batches/$batchId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getExpiredBatches(token: String): List<ProductBatchResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/batches/expired") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun writeOffExpiredBatches(token: String): Map<String, String> {
        return client.post("${BuildConfig.API_BASE_URL}/batches/write-off-expired") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}