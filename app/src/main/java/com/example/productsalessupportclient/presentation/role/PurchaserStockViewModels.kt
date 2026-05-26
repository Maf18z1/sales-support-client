package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.CategoryHistoryPointResponse
import com.example.productsalessupportclient.data.network.StockBatchResponse
import com.example.productsalessupportclient.data.network.StockBatchUpsertRequest
import com.example.productsalessupportclient.data.network.StockDetailResponse
import com.example.productsalessupportclient.data.network.StockDetailUpsertRequest
import com.example.productsalessupportclient.data.network.StockOverviewResponse
import com.example.productsalessupportclient.data.repository.PurchaserStockRepository
import kotlinx.coroutines.launch

data class PurchaserStockUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val items: List<StockOverviewResponse> = emptyList()
)

class PurchaserStockViewModel(
    private val repository: PurchaserStockRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserStockUiState())
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
                val items = repository.loadOverview(token, category, minStock, maxStock, expiringDays)
                uiState = uiState.copy(isLoading = false, items = items)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Failed to load stock")
            }
        }
    }
}

class PurchaserStockViewModelFactory(
    private val repository: PurchaserStockRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserStockViewModel::class.java)) {
            return PurchaserStockViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class PurchaserStockEditUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val detail: StockDetailResponse? = null,
    val batches: List<StockBatchResponse> = emptyList()
)

class PurchaserStockEditViewModel(
    private val repository: PurchaserStockRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserStockEditUiState())
        private set

    fun load(assortmentId: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val detail = repository.loadDetail(token, assortmentId)
                val batches = repository.loadBatches(token, assortmentId)
                uiState = uiState.copy(isLoading = false, detail = detail, batches = batches)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Failed to load detail")
            }
        }
    }

    fun saveDetail(
        assortmentId: Long,
        request: StockDetailUpsertRequest,
        onSaved: () -> Unit
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                repository.updateDetail(token, assortmentId, request)
                uiState = uiState.copy(isLoading = false)
                onSaved()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Save failed"
                )
            }
        }
    }

    fun createBatch(
        request: StockBatchUpsertRequest,
        assortmentId: Long,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.createBatch(token, request)
                load(assortmentId)
                onDone()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Failed to create batch")
            }
        }
    }

    fun updateBatch(
        batchId: Long,
        request: StockBatchUpsertRequest,
        assortmentId: Long,
        onDone: () -> Unit
    ) {
        viewModelScope.launch {
            try {
                repository.updateBatch(token, batchId, request)
                load(assortmentId)
                onDone()
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Failed to update batch")
            }
        }
    }

    fun deleteBatch(
        batchId: Long,
        assortmentId: Long
    ) {
        viewModelScope.launch {
            try {
                repository.deleteBatch(token, batchId)
                load(assortmentId)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Failed to delete batch")
            }
        }
    }
}

class PurchaserStockEditViewModelFactory(
    private val repository: PurchaserStockRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserStockEditViewModel::class.java)) {
            return PurchaserStockEditViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class PurchaserStockAnalyticsUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val categories: List<String> = emptyList(),
    val history: List<CategoryHistoryPointResponse> = emptyList()
)

class PurchaserStockAnalyticsViewModel(
    private val repository: PurchaserStockRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserStockAnalyticsUiState())
        private set

    fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = repository.loadCategories(token)
                uiState = uiState.copy(categories = categories)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Failed to load categories")
            }
        }
    }

    fun loadHistory(category: String) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val history = repository.loadCategoryHistory(token, category)
                uiState = uiState.copy(isLoading = false, history = history)
            } catch (e: Exception) {
                uiState = uiState.copy(isLoading = false, error = e.message ?: "Failed to load history")
            }
        }
    }
}

class PurchaserStockAnalyticsViewModelFactory(
    private val repository: PurchaserStockRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserStockAnalyticsViewModel::class.java)) {
            return PurchaserStockAnalyticsViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}