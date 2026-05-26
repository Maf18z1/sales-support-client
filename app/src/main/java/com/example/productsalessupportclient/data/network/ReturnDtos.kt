package com.example.productsalessupportclient.data.network

import kotlinx.serialization.Serializable

@Serializable
data class ReturnItemResponse(
    val orderItemId: Long,
    val assortmentId: Long,
    val assortmentName: String,
    val quantity: Int,
    val price: Double
)

@Serializable
data class ReturnResponse(
    val id: Long,
    val returnNumber: String,
    val orderId: Long,
    val orderNumber: String,
    val returnDate: String,
    val reason: String?,
    val items: List<ReturnItemResponse>
)

@Serializable
data class CreateReturnItemRequest(
    val orderItemId: Long,
    val quantity: Int
)

@Serializable
data class CreateReturnRequest(
    val orderId: Long,
    val reason: String? = null,
    val items: List<CreateReturnItemRequest>
)

@Serializable
data class WriteOffExpiredBatchesResponse(
    val deletedCount: Int
)