package com.example.productsalessupportclient.data.network

import kotlinx.serialization.Serializable

@Serializable
data class SupplierOrderSummaryResponse(
    val id: Long,
    val orderNumber: String,
    val orderDate: String,
    val status: String,
    val supplierId: Long,
    val supplierName: String,
    val itemsList: String?
)