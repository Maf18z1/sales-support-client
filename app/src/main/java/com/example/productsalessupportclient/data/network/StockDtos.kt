package com.example.productsalessupportclient.data.network

import kotlinx.serialization.Serializable

@Serializable
data class StockOverviewResponse(
    val assortmentId: Long,
    val name: String,
    val category: String? = null,
    val article: String? = null,
    val price: Double,
    val stockQuantity: Int,
    val lastUpdated: String,
    val nearestExpiryDate: String? = null,
    val nearestExpiryDays: Int? = null
)

@Serializable
data class StockDetailResponse(
    val assortmentId: Long,
    val name: String,
    val category: String? = null,
    val article: String? = null,
    val price: Double,
    val stockQuantity: Int,
    val lastUpdated: String,
    val batches: List<StockBatchResponse>
)

@Serializable
data class StockBatchResponse(
    val batchId: Long,
    val assortmentId: Long,
    val quantity: Int,
    val expiryDate: String,
    val receivedDate: String
)

@Serializable
data class StockBatchUpsertRequest(
    val assortmentId: Long,
    val quantity: Int,
    val expiryDate: String,
    val receivedDate: String? = null
)

@Serializable
data class StockDetailUpsertRequest(
    val name: String,
    val category: String? = null,
    val article: String? = null,
    val price: Double,
    val stockQuantity: Int
)

@Serializable
data class CategoryHistoryPointResponse(
    val date: String,
    val quantity: Int
)