package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.network.StockResponse
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import kotlinx.coroutines.launch

data class PurchaserMainUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val expiringSoon: List<ProductBatchResponse> = emptyList(),
    val criticalLowStock: List<StockResponse> = emptyList()
)

class PurchaserMainViewModel(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserMainUiState())
        private set

    init {
        load(days = 30)
    }

    fun load(days: Int) {
        viewModelScope.launch {

            uiState = uiState.copy(
                isLoading = true,
                error = null
            )

            try {
                val expiringSoon = repository.loadExpiringSoon(
                    token = token,
                    days = days
                )

                val criticalLow = repository.loadCriticalLowStock(token)

                uiState = uiState.copy(
                    isLoading = false,
                    expiringSoon = expiringSoon,
                    criticalLowStock = criticalLow
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

class PurchaserMainViewModelFactory(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserMainViewModel::class.java)) {
            return PurchaserMainViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}