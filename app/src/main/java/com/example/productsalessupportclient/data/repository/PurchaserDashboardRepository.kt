package com.example.productsalessupportclient.data.repository

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.productsalessupportclient.data.network.AssortmentSalesPointResponse
import com.example.productsalessupportclient.data.network.AssortmentUpsertRequest
import com.example.productsalessupportclient.data.network.AssortmentWithStockResponse
import com.example.productsalessupportclient.data.network.DashboardApi
import com.example.productsalessupportclient.data.network.StockResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.CreateSupplierOrderRequest
import com.example.productsalessupportclient.data.network.SupplierCatalogItemResponse
import com.example.productsalessupportclient.data.network.SupplierOrderDetailResponse
import com.example.productsalessupportclient.data.network.SupplierOrderSummaryResponse
import com.example.productsalessupportclient.data.network.SupplierResponse
import java.time.LocalDateTime

class PurchaserDashboardRepository(
    private val api: DashboardApi,
    private val promoApi: ClientManagerDashboardApi
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun loadExpiringSoon(
        token: String,
        days: Int
    ): List<ProductBatchResponse> {
        return promoApi.getPromotionProducts(token, days)
            .sortedBy {
                runCatching { LocalDateTime.parse(it.expiryDate) }
                    .getOrNull() ?: LocalDateTime.MAX
            }
    }

    suspend fun loadCriticalLowStock(token: String): List<StockResponse> {
        return api.getLowStock(token, threshold = 5)
            .sortedBy { it.totalQuantity }
    }

    suspend fun loadAssortment(
        token: String,
        category: String?,
        minStock: Int?,
        maxStock: Int?,
        expiringDays: Int?
    ): List<AssortmentWithStockResponse> {
        val assortment = api.getAssortment(token, category)
        val expiringSet = expiringDays?.let { days ->
            promoApi.getPromotionProducts(token, days)
                .map { it.assortmentId }
                .toSet()
        }

        return assortment
            .filter { item ->
                val stock = item.stockQuantity ?: 0
                (minStock == null || stock >= minStock) &&
                        (maxStock == null || stock <= maxStock) &&
                        (expiringSet == null || item.id in expiringSet)
            }
            .sortedBy { it.name }
    }

    suspend fun getAssortmentById(token: String, id: Long) =
        api.getAssortmentById(token, id)

    suspend fun updateAssortment(
        token: String,
        id: Long,
        request: AssortmentUpsertRequest
    ) = api.updateAssortment(token, id, request)

    suspend fun deleteAssortment(token: String, id: Long) =
        api.deleteAssortment(token, id)

    suspend fun setStock(token: String, assortmentId: Long, quantity: Int) =
        api.setStock(token, assortmentId, quantity)

    suspend fun getAssortmentSalesHistory(token: String, id: Long): List<AssortmentSalesPointResponse> =
        api.getAssortmentSalesHistory(token, id)

    suspend fun loadSuppliers(token: String): List<SupplierResponse> =
        api.getSuppliers(token)

    suspend fun loadSupplierProducts(token: String, supplierId: Long): List<SupplierCatalogItemResponse> =
        api.getSupplierProducts(token, supplierId)

    suspend fun loadOrders(
        token: String,
        status: String?,
        supplierId: Long?,
        dateFrom: String?,
        dateTo: String?
    ): List<SupplierOrderSummaryResponse> =
        api.getSupplierOrders(token, status, supplierId, dateFrom, dateTo)

    suspend fun loadOrder(token: String, id: Long): SupplierOrderDetailResponse =
        api.getSupplierOrder(token, id)

    suspend fun createOrder(
        token: String,
        request: CreateSupplierOrderRequest
    ): SupplierOrderDetailResponse =
        api.createSupplierOrder(token, request)
}