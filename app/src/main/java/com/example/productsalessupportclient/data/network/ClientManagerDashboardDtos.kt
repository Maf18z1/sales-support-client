package com.example.productsalessupportclient.data.network

import kotlinx.serialization.Serializable

@Serializable
data class ProductBatchResponse(
    val id: Long,
    val assortmentId: Long,
    val assortmentName: String,
    val quantity: Int,
    val expiryDate: String,
    val receivedDate: String
)

@Serializable
data class ClientOrderResponse(
    val id: Long,
    val orderNumber: String,
    val orderDate: String,
    val status: String,
    val totalAmount: Double,
    val source: String?
)

@Serializable
data class ManagerAssortmentResponse(
    val id: Long,
    val name: String,
    val price: Double,
    val category: String? = null,
    val article: String? = null,
    val stockQuantity: Int? = null
)

@Serializable
data class ManagerClientOrderItemResponse(
    val orderItemId: Long,
    val assortmentId: Long,
    val assortmentName: String,
    val quantity: Int,
    val price: Double
)

@Serializable
data class ManagerClientOrderDetailResponse(
    val id: Long,
    val orderNumber: String,
    val orderDate: String,
    val status: String,
    val totalAmount: Double,
    val source: String?,
    val items: List<ManagerClientOrderItemResponse>
)

@Serializable
data class ClientResponse(
    val id: Long,
    val fullName: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String,
    val birthDate: String? = null,
    val preferredContactMethod: String? = null,
    val notes: String? = null
)

@Serializable
data class CreateManagerClientOrderItemRequest(
    val assortmentId: Long,
    val quantity: Int
)

@Serializable
data class CreateManagerClientOrderRequest(
    val clientId: Long,
    val items: List<CreateManagerClientOrderItemRequest>,
    val source: String = "manual"
)