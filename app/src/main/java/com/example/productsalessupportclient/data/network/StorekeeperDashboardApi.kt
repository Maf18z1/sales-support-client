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
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class StorekeeperDashboardApi {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getSupplierOrders(
        token: String,
        status: String? = null,
        supplierId: Long? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): List<SupplierOrderSummaryResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/supplier-orders") {
            header(HttpHeaders.Authorization, "Bearer $token")
            status?.takeIf { it.isNotBlank() }?.let { parameter("status", it) }
            supplierId?.let { parameter("supplierId", it) }
            dateFrom?.takeIf { it.isNotBlank() }?.let { parameter("dateFrom", it) }
            dateTo?.takeIf { it.isNotBlank() }?.let { parameter("dateTo", it) }
        }.body()
    }

    suspend fun getSupplierOrderDetail(
        token: String,
        id: Long
    ): SupplierOrderDetailResponse {
        return client.get("${BuildConfig.API_BASE_URL}/supplier-orders/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun receiveSupplierOrder(
        token: String,
        id: Long,
        invoiceNumber: String? = null
    ): SupplierOrderDetailResponse {
        return client.post("${BuildConfig.API_BASE_URL}/supplier-orders/$id/receive") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(ReceiveSupplierOrderRequest(invoiceNumber = invoiceNumber))
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

    suspend fun getClientOrderDetail(
        token: String,
        id: Long
    ): ManagerClientOrderDetailResponse {
        return client.get("${BuildConfig.API_BASE_URL}/client-orders/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun reserveClientOrder(token: String, id: Long) {
        client.patch("${BuildConfig.API_BASE_URL}/client-orders/$id/reserve") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun shipClientOrder(token: String, id: Long) {
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

    suspend fun getReturns(token: String): List<ReturnResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/returns") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getExpiredBatches(token: String): List<ProductBatchResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/batches/expired") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun deleteBatch(
        token: String,
        batchId: Long
    ): Map<String, Boolean> {
        return client.delete("${BuildConfig.API_BASE_URL}/batches/$batchId") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun writeOffExpiredBatches(
        token: String
    ): WriteOffExpiredBatchesResponse {
        return client.post("${BuildConfig.API_BASE_URL}/batches/write-off-expired") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }
}