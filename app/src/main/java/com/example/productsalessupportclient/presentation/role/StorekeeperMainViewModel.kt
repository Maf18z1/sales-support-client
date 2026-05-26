package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.network.SupplierOrderSummaryResponse
import com.example.productsalessupportclient.data.repository.StorekeeperDashboardRepository
import kotlinx.coroutines.launch

data class StorekeeperMainUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val supplierOrders: List<SupplierOrderSummaryResponse> = emptyList(),
    val writeOffBatches: List<ProductBatchResponse> = emptyList(),
    val readyOrders: List<ClientOrderResponse> = emptyList()
)

class StorekeeperMainViewModel(
    private val repository: StorekeeperDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(StorekeeperMainUiState())
        private set

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)

            try {
                val supplierOrders = repository.loadSupplierOrders(token)
                val writeOffBatches = repository.loadExpiredBatches(token)
                val readyOrders = repository.loadClientOrders(token, status = "confirmed")

                uiState = uiState.copy(
                    isLoading = false,
                    supplierOrders = supplierOrders,
                    writeOffBatches = writeOffBatches,
                    readyOrders = readyOrders
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load dashboard data"
                )
            }
        }
    }
}

class StorekeeperMainViewModelFactory(
    private val repository: StorekeeperDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StorekeeperMainViewModel::class.java)) {
            return StorekeeperMainViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}