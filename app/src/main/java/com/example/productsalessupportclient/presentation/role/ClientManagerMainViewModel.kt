package com.example.productsalessupportclient.presentation.role

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.repository.ClientManagerDashboardRepository
import kotlinx.coroutines.launch

data class ClientManagerUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val promotionProducts: List<ProductBatchResponse> = emptyList(),
    val pendingOrders: List<ClientOrderResponse> = emptyList()
)

class ClientManagerMainViewModel(
    private val repository: ClientManagerDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by mutableStateOf(ClientManagerUiState())
        private set

    init {
        load(30)
    }

    fun load(days: Int) {

        viewModelScope.launch {

            uiState = uiState.copy(
                isLoading = true,
                error = null
            )

            try {

                val promotions =
                    repository.loadPromotionProducts(
                        token = token,
                        days = days
                    )

                val orders =
                    repository.loadPendingOrders(token)

                uiState = uiState.copy(
                    isLoading = false,
                    promotionProducts = promotions,
                    pendingOrders = orders
                )

            } catch (e: Exception) {

                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Ошибка загрузки"
                )
            }
        }
    }
}

class ClientManagerMainViewModelFactory(
    private val repository: ClientManagerDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {

        if (modelClass.isAssignableFrom(ClientManagerMainViewModel::class.java)) {

            return ClientManagerMainViewModel(
                repository,
                token
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}