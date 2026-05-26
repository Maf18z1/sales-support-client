package com.example.productsalessupportclient.data.network

import kotlinx.serialization.Serializable

@Serializable
data class AssortmentWithStockResponse(
    val id: Long,
    val name: String,
    val price: Double,
    val category: String?,
    val article: String?,
    val stockQuantity: Int?
)

@Serializable
data class AssortmentUpsertRequest(
    val name: String,
    val price: Double,
    val category: String? = null,
    val article: String? = null,
    val initialQuantity: Int? = null
)

@Serializable
data class StockSetRequest(
    val quantity: Int
)

@Serializable
data class AssortmentSalesPointResponse(
    val purchaseDate: String,
    val assortmentName: String,
    val quantity: Int
)