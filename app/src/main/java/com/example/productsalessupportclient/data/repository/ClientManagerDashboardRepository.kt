package com.example.productsalessupportclient.data.repository

import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ClientResponse
import com.example.productsalessupportclient.data.network.CreateManagerClientOrderRequest
import com.example.productsalessupportclient.data.network.ManagerAssortmentResponse
import com.example.productsalessupportclient.data.network.ManagerClientOrderDetailResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse

class ClientManagerDashboardRepository(
    private val api: ClientManagerDashboardApi
) {

    suspend fun loadPromotionProducts(
        token: String,
        days: Int
    ): List<ProductBatchResponse> {
        return api.getPromotionProducts(
            token = token,
            days = days
        ).sortedBy { it.expiryDate }
    }

    suspend fun loadOrders(
        token: String,
        status: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): List<ClientOrderResponse> {
        return api.getClientOrders(
            token = token,
            status = status,
            dateFrom = dateFrom,
            dateTo = dateTo
        ).sortedWith(
            compareByDescending<ClientOrderResponse> { it.orderDate }
                .thenByDescending { it.id }
        )
    }

    suspend fun loadPendingOrders(
        token: String
    ): List<ClientOrderResponse> {
        return loadOrders(token, status = "new")
    }

    suspend fun loadAllOrders(
        token: String
    ): List<ClientOrderResponse> {
        return loadOrders(token)
    }

    suspend fun loadOrdersByStatus(
        token: String,
        status: String,
        dateFrom: String? = null,
        dateTo: String? = null
    ): List<ClientOrderResponse> {
        return loadOrders(
            token = token,
            status = status,
            dateFrom = dateFrom,
            dateTo = dateTo
        )
    }

    suspend fun loadOrderDetail(
        token: String,
        id: Long
    ): ManagerClientOrderDetailResponse {
        return api.getClientOrderDetail(token, id)
    }

    suspend fun confirmOrder(
        token: String,
        id: Long
    ) {
        api.confirmClientOrder(token, id)
    }

    suspend fun reserveOrder(
        token: String,
        id: Long
    ) {
        api.reserveClientOrder(token, id)
    }

    suspend fun shipOrder(
        token: String,
        id: Long
    ) {
        api.shipClientOrder(token, id)
    }

    suspend fun cancelOrder(
        token: String,
        orderId: Long,
        reason: String? = null
    ) {
        api.cancelClientOrder(token, orderId, reason)
    }

    suspend fun createOrder(
        token: String,
        request: CreateManagerClientOrderRequest
    ): Long {
        val response = api.createClientOrder(token, request)
        return response["orderId"] ?: error("orderId missing in response")
    }

    suspend fun loadAssortment(
        token: String
    ): List<ManagerAssortmentResponse> {
        return api.getManagerAssortment(token)
            .sortedBy { it.name }
    }

    suspend fun loadClients(
        token: String
    ): List<ClientResponse> {
        return api.getClients(token)
            .sortedBy { it.fullName }
    }
}