package com.example.productsalessupportclient.data.repository

import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.network.ReturnResponse
import com.example.productsalessupportclient.data.network.StorekeeperDashboardApi
import com.example.productsalessupportclient.data.network.SupplierOrderDetailResponse
import com.example.productsalessupportclient.data.network.SupplierOrderSummaryResponse

class StorekeeperDashboardRepository(
    private val api: StorekeeperDashboardApi
) {
    suspend fun loadSupplierOrders(
        token: String,
        status: String? = null,
        supplierId: Long? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ): List<SupplierOrderSummaryResponse> {
        return api.getSupplierOrders(
            token = token,
            status = status,
            supplierId = supplierId,
            dateFrom = dateFrom,
            dateTo = dateTo
        ).sortedWith(
            compareByDescending<SupplierOrderSummaryResponse> { it.orderDate }
                .thenByDescending { it.id }
        )
    }

    suspend fun loadSupplierOrderDetail(
        token: String,
        id: Long
    ): SupplierOrderDetailResponse {
        return api.getSupplierOrderDetail(token, id)
    }

    suspend fun receiveSupplierOrder(
        token: String,
        id: Long,
        invoiceNumber: String? = null
    ): SupplierOrderDetailResponse {
        return api.receiveSupplierOrder(token, id, invoiceNumber)
    }

    suspend fun loadClientOrders(
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

    suspend fun loadClientOrderDetail(
        token: String,
        id: Long
    ) = api.getClientOrderDetail(token, id)

    suspend fun reserveClientOrder(
        token: String,
        id: Long
    ) {
        api.reserveClientOrder(token, id)
    }

    suspend fun shipClientOrder(
        token: String,
        id: Long
    ) {
        api.shipClientOrder(token, id)
    }

    suspend fun cancelClientOrder(
        token: String,
        orderId: Long,
        reason: String? = null
    ): ReturnResponse {
        return api.cancelClientOrder(token, orderId, reason)
    }

    suspend fun loadReturns(token: String): List<ReturnResponse> {
        return api.getReturns(token)
    }

    suspend fun loadExpiredBatches(token: String): List<ProductBatchResponse> {
        return api.getExpiredBatches(token)
    }

    suspend fun deleteBatch(
        token: String,
        batchId: Long
    ): Map<String, Boolean> {
        return api.deleteBatch(token, batchId)
    }

    suspend fun writeOffExpiredBatches(token: String): Int {
        return api.writeOffExpiredBatches(token).deletedCount
    }
}