package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.productsalessupportclient.data.network.SupplierOrderDetailResponse
import com.example.productsalessupportclient.data.network.SupplierOrderItemResponse
import com.example.productsalessupportclient.data.network.SupplierOrderSummaryResponse
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.StorekeeperDashboardRepository
import kotlinx.coroutines.launch
import java.time.LocalDate

private enum class SupplierOrderStatusFilter(
    val value: String?,
    val title: String
) {
    ALL(null, "Все"),
    NEW("new", "Новый"),
    CREATED("sent", "Отправлен"),
    IN_TRANSIT("in_transit", "В пути"),
    RECEIVED("received", "Получен")
}

data class StorekeeperSupplierOrdersUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val orders: List<SupplierOrderSummaryResponse> = emptyList(),
    val detail: SupplierOrderDetailResponse? = null
)

class StorekeeperSupplierOrdersViewModel(
    private val repository: StorekeeperDashboardRepository,
    private val token: String
) : ViewModel() {

    var uiState by androidx.compose.runtime.mutableStateOf(StorekeeperSupplierOrdersUiState())
        private set

    fun load(
        status: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val orders = repository.loadSupplierOrders(token, status, dateFrom = dateFrom, dateTo = dateTo)
                uiState = uiState.copy(
                    isLoading = false,
                    orders = orders
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Не удалось загрузить заказы поставщикам"
                )
            }
        }
    }

    fun loadDetail(orderId: Long) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                val detail = repository.loadSupplierOrderDetail(token, orderId)
                uiState = uiState.copy(
                    isLoading = false,
                    detail = detail
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Не удалось загрузить детали заказа"
                )
            }
        }
    }

    fun receiveOrder(
        orderId: Long,
        status: String? = null,
        dateFrom: String? = null,
        dateTo: String? = null
    ) {
        viewModelScope.launch {
            uiState = uiState.copy(isLoading = true, error = null)
            try {
                repository.receiveSupplierOrder(token, orderId)
                val orders = repository.loadSupplierOrders(token, status, dateFrom = dateFrom, dateTo = dateTo)
                uiState = uiState.copy(
                    isLoading = false,
                    orders = orders
                )
            } catch (e: Exception) {
                uiState = uiState.copy(
                    isLoading = false,
                    error = e.message ?: "Не удалось принять заказ"
                )
            }
        }
    }
}

class StorekeeperSupplierOrdersViewModelFactory(
    private val repository: StorekeeperDashboardRepository,
    private val token: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StorekeeperSupplierOrdersViewModel::class.java)) {
            return StorekeeperSupplierOrdersViewModel(repository, token) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun StorekeeperSupplierOrdersScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val repository = remember { StorekeeperDashboardRepository(com.example.productsalessupportclient.data.network.StorekeeperDashboardApi()) }
    val vm: StorekeeperSupplierOrdersViewModel = viewModel(
        factory = StorekeeperSupplierOrdersViewModelFactory(repository, session.token)
    )

    val state = vm.uiState
    val navController = rememberNavController()

    var showFilters by rememberSaveable { mutableStateOf(false) }
    var statusInput by rememberSaveable {
        mutableStateOf(SupplierOrderStatusFilter.ALL)
    }
    var dateFromInput by rememberSaveable { mutableStateOf("") }
    var dateToInput by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(Unit) {
        vm.load()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = { showFilters = !showFilters }) {
                Text(if (showFilters) "Скрыть фильтры" else "Показать фильтры")
            }
        }

        if (showFilters) {
            Spacer(modifier = Modifier.height(10.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Фильтры", style = MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Column(modifier = Modifier.width(160.dp)) {

                            Text(
                                text = "Статус",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            SupplierStatusDropdownField(
                                value = statusInput,
                                onValueChange = {
                                    statusInput = it
                                },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dateFromInput,
                            onValueChange = { dateFromInput = it },
                            label = { Text("Дата от (yyyy-MM-dd)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dateToInput,
                            onValueChange = { dateToInput = it },
                            label = { Text("Дата до (yyyy-MM-dd)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = {
                            vm.load(
                                status = statusInput.value,
                                dateFrom = dateFromInput.trim().takeIf { it.isNotBlank() },
                                dateTo = dateToInput.trim().takeIf { it.isNotBlank() }
                            )
                        }) {
                            Text("Применить")
                        }

                        OutlinedButton(onClick = {
                            statusInput = SupplierOrderStatusFilter.ALL
                            dateFromInput = ""
                            dateToInput = ""
                            vm.load()
                        }) {
                            Text("Сбросить")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        NavHost(navController = navController, startDestination = "list") {
            composable("list") {
                DashboardSection(
                    modifier = Modifier.fillMaxSize(),
                    title = "Ожидаемые поступления от поставщиков",
                    titleFontSize = 10.sp,
                    headers = listOf("Номер", "Поставщик", "Статус", "Дата"),
                    headerFontSize = 11.sp,
                    items = state.orders,
                    emptyText = "Нет поступлений"
                ) { index, item ->
                    SupplierOrderRowCard(
                        index = index,
                        item = item,
                        onInfoClick = { navController.navigate("detail/${item.id}") },
                        onReceiveClick = {
                            vm.receiveOrder(
                                orderId = item.id,
                                status = statusInput.value,
                                dateFrom = dateFromInput.trim().takeIf { it.isNotBlank() },
                                dateTo = dateToInput.trim().takeIf { it.isNotBlank() }
                            )
                        }
                    )
                }
            }

            composable("detail/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                LaunchedEffect(id) { vm.loadDetail(id) }
                SupplierOrderDetailScreen(
                    detail = state.detail,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun SupplierStatusDropdownField(
    value: SupplierOrderStatusFilter,
    onValueChange: (SupplierOrderStatusFilter) -> Unit,
    modifier: Modifier = Modifier
) {

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {

        OutlinedButton(
            onClick = {
                expanded = true
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(value.title)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            SupplierOrderStatusFilter.entries.forEach { option ->

                DropdownMenuItem(
                    text = {
                        Text(option.title)
                    },
                    onClick = {
                        expanded = false
                        onValueChange(option)
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierOrderRowCard(
    index: Int,
    item: SupplierOrderSummaryResponse,
    onInfoClick: () -> Unit,
    onReceiveClick: () -> Unit
) {
    val colors = listOf(
        Color(0xFFEAF2FF),
        Color(0xFFF2ECFF),
        Color(0xFFFFF2E8),
        Color(0xFFEAF8EF)
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = colors[index % colors.size])
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(2.2f)) {
                Text(item.orderNumber, maxLines = 1, overflow = TextOverflow.Ellipsis,fontSize = 11.sp)
            }
            Box(modifier = Modifier.weight(2.2f)) {
                Text(item.supplierName, maxLines = 1, overflow = TextOverflow.Ellipsis,fontSize = 11.sp)
            }
            Box(modifier = Modifier.weight(1.2f)) {
                Text(item.status, maxLines = 1, overflow = TextOverflow.Ellipsis,fontSize = 11.sp)
            }
            Box(modifier = Modifier.weight(1.2f)) {
                Text(item.orderDate, maxLines = 1, overflow = TextOverflow.Ellipsis,fontSize = 11.sp)
            }

            IconButton(onClick = onInfoClick) {
                Icon(Icons.Filled.Info, contentDescription = "Информация")
            }

            if (item.status == "in_transit") {
                IconButton(onClick = onReceiveClick) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "Принять")
                }
            }
        }
    }
}

@Composable
private fun SupplierOrderDetailScreen(
    detail: SupplierOrderDetailResponse?,
    onBack: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text("Детали заказа поставщику", style = MaterialTheme.typography.headlineSmall)
        Spacer(modifier = Modifier.height(12.dp))

        if (detail == null) {
            Text("Загрузка...")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Номер: ${detail.orderNumber}")
                    Text("Поставщик: ${detail.supplierName}")
                    Text("Дата: ${detail.orderDate}")
                    Text("Статус: ${detail.status}")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Товары в заказе", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            detail.items.forEach { item ->
                SupplierOrderItemCard(item)
                Spacer(modifier = Modifier.height(8.dp))
            }
        }

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}

@Composable
private fun SupplierOrderItemCard(item: SupplierOrderItemResponse) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(modifier = Modifier.weight(1.6f)) {
                Text(item.assortmentName)
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Text("Кол-во: ${item.quantity}")
            }
            Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.CenterEnd) {
                Text(item.status)
            }
        }
    }
}