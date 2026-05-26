package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.CreateSupplierOrderRequest
import com.example.productsalessupportclient.data.network.SupplierCatalogItemResponse
import com.example.productsalessupportclient.data.network.SupplierOrderDetailResponse
import com.example.productsalessupportclient.data.network.SupplierOrderSummaryResponse
import com.example.productsalessupportclient.data.network.SupplierResponse
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import kotlinx.coroutines.launch

data class PurchaserSupplierOrdersUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val suppliers: List<SupplierResponse> = emptyList(),
    val orders: List<SupplierOrderSummaryResponse> = emptyList()
)

class PurchaserSupplierOrdersViewModel(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserSupplierOrdersUiState())
        private set

    init {
        loadSuppliers()
        loadOrders(null, null, null, null)
    }

    fun loadSuppliers() {
        viewModelScope.launch {
            try {
                val suppliers = repository.loadSuppliers(token)
                uiState = uiState.copy(suppliers = suppliers)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Failed to load suppliers")
            }
        }
    }

    fun loadOrders(
        status: String?,
        supplierId: Long?,
        dateFrom: String?,
        dateTo: String?
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val orders = repository.loadOrders(token, status, supplierId, dateFrom, dateTo)
                uiState = uiState.copy(isLoading = false, orders = orders)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load supplier orders"
                )
            }
        }
    }
}

class PurchaserSupplierOrdersViewModelFactory(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserSupplierOrdersViewModel::class.java)) {
            return PurchaserSupplierOrdersViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class PurchaserSupplierOrderCreateUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val suppliers: List<SupplierResponse> = emptyList(),
    val products: List<SupplierCatalogItemResponse> = emptyList()
)

class PurchaserSupplierOrderCreateViewModel(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserSupplierOrderCreateUiState())
        private set

    fun loadSuppliers() {
        viewModelScope.launch {
            try {
                val suppliers = repository.loadSuppliers(token)
                uiState = uiState.copy(suppliers = suppliers)
            } catch (e: Exception) {
                uiState = uiState.copy(error = e.message ?: "Failed to load suppliers")
            }
        }
    }

    fun loadProducts(supplierId: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val products = repository.loadSupplierProducts(token, supplierId)
                uiState = uiState.copy(isLoading = false, products = products)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load supplier products"
                )
            }
        }
    }

    fun createOrder(
        request: CreateSupplierOrderRequest,
        onSuccess: (Long) -> Unit
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val created = repository.createOrder(token, request)
                uiState = uiState.copy(isLoading = false)
                onSuccess(created.id)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create supplier order"
                )
            }
        }
    }
}

class PurchaserSupplierOrderCreateViewModelFactory(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserSupplierOrderCreateViewModel::class.java)) {
            return PurchaserSupplierOrderCreateViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

data class PurchaserSupplierOrderInfoUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val order: SupplierOrderDetailResponse? = null
)

class PurchaserSupplierOrderInfoViewModel(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(PurchaserSupplierOrderInfoUiState())
        private set

    fun load(id: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val order = repository.loadOrder(token, id)
                uiState = uiState.copy(isLoading = false, order = order)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load supplier order"
                )
            }
        }
    }
}

class PurchaserSupplierOrderInfoViewModelFactory(
    private val repository: PurchaserDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PurchaserSupplierOrderInfoViewModel::class.java)) {
            return PurchaserSupplierOrderInfoViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}