package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.AssortmentUpsertRequest
import com.example.productsalessupportclient.data.network.AssortmentWithStockResponse
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import kotlinx.coroutines.launch

data class PurchaserAssortmentEditUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val item: AssortmentWithStockResponse? = null
)

class PurchaserAssortmentEditViewModel(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserAssortmentEditUiState())
        private set

    fun load(id: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val item = repository.getAssortmentById(token, id)
                uiState = uiState.copy(isLoading = false, item = item)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Load failed")
            }
        }
    }

    fun save(
        id: Long,
        request: AssortmentUpsertRequest,
        stockQuantity: Int?,
        onSuccess: () -> Unit
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                repository.updateAssortment(token, id, request)
                stockQuantity?.let { repository.setStock(token, id, it) }
                uiState = uiState.copy(isLoading = false)
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Save failed")
            }
        }
    }
}

class PurchaserAssortmentEditViewModelFactory(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserAssortmentEditViewModel::class.java)) {
            return PurchaserAssortmentEditViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}