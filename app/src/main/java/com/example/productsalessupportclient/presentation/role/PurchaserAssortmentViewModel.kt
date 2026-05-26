package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.AssortmentWithStockResponse
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import kotlinx.coroutines.launch

data class PurchaserAssortmentUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<AssortmentWithStockResponse> = emptyList()
)

class PurchaserAssortmentViewModel(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserAssortmentUiState())
        private set

    fun load(
        category: String?,
        minStock: Int?,
        maxStock: Int?,
        expiringDays: Int?
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val items = repository.loadAssortment(
                    token = token,
                    category = category,
                    minStock = minStock,
                    maxStock = maxStock,
                    expiringDays = expiringDays
                )
                uiState = uiState.copy(isLoading = false, items = items)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load assortment"
                )
            }
        }
    }

    fun deleteAssortment(id: Long, onDone: () -> Unit = {}) {
        viewModelScope.launch {
            try {
                repository.deleteAssortment(token, id)
                onDone()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Delete failed")
            }
        }
    }
}

class PurchaserAssortmentViewModelFactory(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserAssortmentViewModel::class.java)) {
            return PurchaserAssortmentViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}