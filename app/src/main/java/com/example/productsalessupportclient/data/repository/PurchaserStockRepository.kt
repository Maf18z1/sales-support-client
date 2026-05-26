package com.example.productsalessupportclient.data.repository

import com.example.productsalessupportclient.data.network.CategoryHistoryPointResponse
import com.example.productsalessupportclient.data.network.ReportApi
import com.example.productsalessupportclient.data.network.StockApi
import com.example.productsalessupportclient.data.network.StockBatchResponse
import com.example.productsalessupportclient.data.network.StockBatchUpsertRequest
import com.example.productsalessupportclient.data.network.StockDetailResponse
import com.example.productsalessupportclient.data.network.StockDetailUpsertRequest
import com.example.productsalessupportclient.data.network.StockOverviewResponse

class PurchaserStockRepository(
    private val stockApi: StockApi,
    private val reportApi: ReportApi
) {
    suspend fun loadOverview(
        token: String,
        category: String?,
        minStock: Int?,
        maxStock: Int?,
        expiringDays: Int?
    ): List<StockOverviewResponse> =
        stockApi.getOverview(token, category, minStock, maxStock, expiringDays)

    suspend fun loadDetail(token: String, assortmentId: Long): StockDetailResponse =
        stockApi.getDetail(token, assortmentId)

    suspend fun updateDetail(
        token: String,
        assortmentId: Long,
        request: StockDetailUpsertRequest
    ): StockDetailResponse =
        stockApi.updateDetail(token, assortmentId, request)

    suspend fun setStock(token: String, assortmentId: Long, quantity: Int) =
        stockApi.setStock(token, assortmentId, quantity)

    suspend fun loadBatches(token: String, assortmentId: Long): List<StockBatchResponse> =
        stockApi.getBatches(token, assortmentId)

    suspend fun createBatch(
        token: String,
        request: StockBatchUpsertRequest
    ) = stockApi.createBatch(token, request)

    suspend fun updateBatch(
        token: String,
        batchId: Long,
        request: StockBatchUpsertRequest
    ) = stockApi.updateBatch(token, batchId, request)

    suspend fun deleteBatch(token: String, batchId: Long) =
        stockApi.deleteBatch(token, batchId)

    suspend fun loadCategoryHistory(
        token: String,
        category: String
    ): List<CategoryHistoryPointResponse> =
        reportApi.getCategoryHistory(token, category)

    suspend fun loadCategories(token: String): List<String> {
        return reportApi.getCategories(token)
            .mapNotNull { it.category }
            .distinct()
            .sorted()
    }
}