package com.example.productsalessupportclient.data.network

import com.example.productsalesupportclient.BuildConfig
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class ClientManagerDashboardApi {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getPromotionProducts(
        token: String,
        days: Int
    ): List<ProductBatchResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/batches/expiring?days=$days") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getClientOrders(
        token: String,
        status: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): List<ClientOrderResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/client-orders") {
            header(HttpHeaders.Authorization, "Bearer $token")
            status?.takeIf { it.isNotBlank() }?.let { parameter("status", it) }
            dateFrom?.takeIf { it.isNotBlank() }?.let { parameter("dateFrom", it) }
            dateTo?.takeIf { it.isNotBlank() }?.let { parameter("dateTo", it) }
        }.body()
    }



    suspend fun getPendingOrders(token: String): List<ClientOrderResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/client-orders/status?value=new") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getAllClientOrders(token: String): List<ClientOrderResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/client-orders") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getClientOrdersByStatus(
        token: String,
        status: String
    ): List<ClientOrderResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/client-orders/status?value=$status") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getClientOrderDetail(
        token: String,
        id: Long
    ): ManagerClientOrderDetailResponse {
        return client.get("${BuildConfig.API_BASE_URL}/client-orders/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun confirmClientOrder(
        token: String,
        id: Long
    ) {
        client.patch("${BuildConfig.API_BASE_URL}/client-orders/$id/confirm") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun reserveClientOrder(
        token: String,
        id: Long
    ) {
        client.patch("${BuildConfig.API_BASE_URL}/client-orders/$id/reserve") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun shipClientOrder(
        token: String,
        id: Long
    ) {
        client.patch("${BuildConfig.API_BASE_URL}/client-orders/$id/ship") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun cancelClientOrder(
        token: String,
        orderId: Long,
        reason: String? = null
    ): ReturnResponse {
        return client.post("${BuildConfig.API_BASE_URL}/returns/order/$orderId/cancel") {
            header(HttpHeaders.Authorization, "Bearer $token")
            reason?.takeIf { it.isNotBlank() }?.let { parameter("reason", it) }
        }.body()
    }

    suspend fun createClientOrder(
        token: String,
        request: CreateManagerClientOrderRequest
    ): Map<String, Long> {
        return client.post("${BuildConfig.API_BASE_URL}/client-orders") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun getManagerAssortment(token: String): List<ManagerAssortmentResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/assortment") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getClients(token: String): List<ClientResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/clients") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}