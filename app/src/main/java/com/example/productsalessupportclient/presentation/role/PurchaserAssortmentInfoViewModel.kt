package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.AssortmentSalesPointResponse
import com.example.productsalessupportclient.data.network.AssortmentWithStockResponse
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import kotlinx.coroutines.launch

data class PurchaserAssortmentInfoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val item: AssortmentWithStockResponse? = null,
    val history: List<AssortmentSalesPointResponse> = emptyList()
)

class PurchaserAssortmentInfoViewModel(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserAssortmentInfoUiState())
        private set

    fun load(id: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val item = repository.getAssortmentById(token, id)
                val history = repository.getAssortmentSalesHistory(token, id)
                uiState = uiState.copy(
                    isLoading = false,
                    item = item,
                    history = history
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Load failed"
                )
            }
        }
    }
}

class PurchaserAssortmentInfoViewModelFactory(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserAssortmentInfoViewModel::class.java)) {
            return PurchaserAssortmentInfoViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}