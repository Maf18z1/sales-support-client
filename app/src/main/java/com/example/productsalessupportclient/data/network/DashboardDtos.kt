package com.example.productsalessupportclient.data.network

import kotlinx.serialization.Serializable

@Serializable
data class StockResponse(
    val stockId: Long,
    val assortmentId: Long,
    val assortmentName: String,
    val article: String?,
    val totalQuantity: Int,
    val lastUpdated: String
)

@Serializable
data class AssortmentResponse(
    val id: Long,
    val name: String,
    val price: Double,
    val category: String?,
    val article: String?,
    val stockQuantity: Int?
)

@Serializable
data class SupplierResponse(
    val id: Long,
    val name: String,
    val phone: String? = null,
    val email: String? = null,
    val address: String? = null
)

@Serializable
data class SupplierCatalogItemResponse(
    val assortmentId: Long,
    val name: String,
    val category: String? = null,
    val article: String? = null,
    val price: Double,
    val stockQuantity: Int? = null
)

@Serializable
data class SupplierOrderItemRequest(
    val assortmentId: Long,
    val quantity: Int,
    val price: Double? = null
)

@Serializable
data class CreateSupplierOrderRequest(
    val supplierId: Long,
    val orderNumber: String? = null,
    val items: List<SupplierOrderItemRequest>,
    val itemsList: String? = null
)

@Serializable
data class ReceiveSupplierOrderRequest(
    val invoiceNumber: String? = null
)

@Serializable
data class SupplierOrderItemResponse(
    val supplierOrderItemId: Long,
    val assortmentId: Long,
    val assortmentName: String,
    val quantity: Int,
    val price: Double,
    val status: String
)

@Serializable
data class SupplierOrderDetailResponse(
    val id: Long,
    val orderNumber: String,
    val orderDate: String,
    val status: String,
    val supplierId: Long,
    val supplierName: String,
    val itemsList: String?,
    val items: List<SupplierOrderItemResponse>
)