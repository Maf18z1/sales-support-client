package com.example.productsalessupportclient.data.network

import androidx.compose.ui.autofill.ContentType
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
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

class DashboardApi {

    private val client = HttpClient(OkHttp) {
        install(ContentNegotiation) {
            json(Json { ignoreUnknownKeys = true })
        }
    }

    suspend fun getStock(token: String): List<StockResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/stock") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getLowStock(token: String, threshold: Int = 5): List<StockResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/stock/low?threshold=$threshold") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getAssortment(
        token: String,
        category: String? = null
    ): List<AssortmentWithStockResponse> {
        val url = buildString {
            append("${BuildConfig.API_BASE_URL}/assortment")
            if (!category.isNullOrBlank()) {
                append("?category=")
                append(category)
            }
        }

        return client.get(url) {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getAssortmentById(token: String, id: Long): AssortmentWithStockResponse {
        return client.get("${BuildConfig.API_BASE_URL}/assortment/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun updateAssortment(
        token: String,
        id: Long,
        request: AssortmentUpsertRequest
    ): AssortmentWithStockResponse {
        return client.put("${BuildConfig.API_BASE_URL}/assortment/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(request)
        }.body()
    }

    suspend fun deleteAssortment(token: String, id: Long) {
        client.delete("${BuildConfig.API_BASE_URL}/assortment/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }
    }

    suspend fun setStock(token: String, assortmentId: Long, quantity: Int): StockResponse {
        return client.post("${BuildConfig.API_BASE_URL}/stock/$assortmentId/set") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(StockSetRequest(quantity))
        }.body()
    }

    suspend fun getAssortmentSalesHistory(
        token: String,
        id: Long
    ): List<AssortmentSalesPointResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/assortment/$id/sales-history") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getSuppliers(token: String): List<SupplierResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/suppliers") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun getSupplierProducts(token: String, supplierId: Long): List<SupplierCatalogItemResponse> {
        return client.get("${BuildConfig.API_BASE_URL}/suppliers/$supplierId/products") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
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

    suspend fun getSupplierOrder(token: String, id: Long): SupplierOrderDetailResponse {
        return client.get("${BuildConfig.API_BASE_URL}/supplier-orders/$id") {
            header(HttpHeaders.Authorization, "Bearer $token")
        }.body()
    }

    suspend fun createSupplierOrder(
        token: String,
        request: CreateSupplierOrderRequest
    ): SupplierOrderDetailResponse {
        return client.post("${BuildConfig.API_BASE_URL}/supplier-orders") {
            header(HttpHeaders.Authorization, "Bearer $token")
            contentType(io.ktor.http.ContentType.Application.Json)
            setBody(request)
        }.body()
    }
}