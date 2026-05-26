package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ClientResponse
import com.example.productsalessupportclient.data.network.CreateManagerClientOrderRequest
import com.example.productsalessupportclient.data.network.ManagerAssortmentResponse
import com.example.productsalessupportclient.data.network.ManagerClientOrderDetailResponse
import com.example.productsalessupportclient.data.repository.ClientManagerDashboardRepository
import kotlinx.coroutines.launch

data class ClientManagerOrdersUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val orders: List<ClientOrderResponse> = emptyList(),
    val orderDetail: ManagerClientOrderDetailResponse? = null,
    val assortment: List<ManagerAssortmentResponse> = emptyList(),
    val clients: List<ClientResponse> = emptyList()
)

class ClientManagerOrdersViewModel(
    private val repository: ClientManagerDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(ClientManagerOrdersUiState())
        private set

    fun loadOrders(statusFilter: String? = null) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val orders = fetchOrders(statusFilter)
                uiState = uiState.copy(
                    isLoading = false,
                    orders = orders
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load orders"
                )
            }
        }
    }

    fun loadOrderDetail(orderId: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val detail = repository.loadOrderDetail(token, orderId)
                uiState = uiState.copy(
                    isLoading = false,
                    orderDetail = detail
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to load order detail"
                )
            }
        }
    }

    fun loadAssortment() {
        viewModelScope.launch {
            try {
                val assortment = repository.loadAssortment(token)
                uiState = uiState.copy(assortment = assortment)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    error = e.message ?: "Failed to load assortment"
                )
            }
        }
    }

    fun confirmOrder(
        orderId: Long,
        statusFilter: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                repository.confirmOrder(token, orderId)
                val orders = fetchOrders(statusFilter)
                uiState = uiState.copy(
                    isLoading = false,
                    orders = orders
                )
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to confirm order"
                )
            }
        }
    }

    fun createOrder(
        request: CreateManagerClientOrderRequest,
        statusFilter: String? = null,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                repository.createOrder(token, request)
                val orders = fetchOrders(statusFilter)
                uiState = uiState.copy(
                    isLoading = false,
                    orders = orders
                )
                onSuccess()
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Failed to create order"
                )
            }
        }
    }

    private suspend fun fetchOrders(statusFilter: String?): List<ClientOrderResponse> {
        return if (statusFilter.isNullOrBlank()) {
            repository.loadAllOrders(token)
        } else {
            repository.loadOrdersByStatus(token, statusFilter)
        }
    }

    fun loadClients() {
        viewModelScope.launch {
            try {
                val clients = repository.loadClients(token)
                uiState = uiState.copy(clients = clients)
            } catch (e: Exception) {
                uiState = uiState.copy(
                    error = e.message ?: "Failed to load clients"
                )
            }
        }
    }
}

class ClientManagerOrdersViewModelFactory(
    private val repository: ClientManagerDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ClientManagerOrdersViewModel::class.java)) {
            return ClientManagerOrdersViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}