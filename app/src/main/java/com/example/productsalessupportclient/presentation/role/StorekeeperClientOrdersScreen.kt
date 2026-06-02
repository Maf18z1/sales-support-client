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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LocalShipping
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.ManagerClientOrderDetailResponse
import com.example.productsalessupportclient.data.network.StorekeeperDashboardApi
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.StorekeeperDashboardRepository
import kotlinx.coroutines.launch

private const val STOREKEEPER_ORDER_TITLE_WEIGHT = 2.2f
private const val STOREKEEPER_ORDER_DATE_WEIGHT = 1.3f
private const val STOREKEEPER_ORDER_STATUS_WEIGHT = 1.2f
private const val STOREKEEPER_ORDER_ACTION_WEIGHT = 2.0f

private enum class StorekeeperClientOrderStatus(
    val value: String?,
    val title: String
) {
    ALL(null, "Все"),
    NEW("new", "Новые"),
    CONFIRMED("confirmed", "Подтвержден"),
    RESERVED("reserved", "Зарезервирован"),
    SHIPPED("shipped", "Отгружен"),
    CANCELLED("cancelled", "Отменен")
}

@Composable
fun StorekeeperClientOrdersScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val repository = remember {
        StorekeeperDashboardRepository(
            StorekeeperDashboardApi()
        )
    }

    val scope = rememberCoroutineScope()
    val navController = rememberNavController()

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    var orderList by rememberSaveable { mutableStateOf(listOf<ClientOrderResponse>()) }

    var statusFilter by rememberSaveable {
        mutableStateOf(StorekeeperClientOrderStatus.ALL)
    }
    var dateFrom by rememberSaveable { mutableStateOf("") }
    var dateTo by rememberSaveable { mutableStateOf("") }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    var selectedOrder by rememberSaveable { mutableStateOf<ManagerClientOrderDetailResponse?>(null) }
    var detailLoading by rememberSaveable { mutableStateOf(false) }
    var actionLoadingId by rememberSaveable { mutableStateOf<Long?>(null) }

    fun reloadOrders() {
        scope.launch {
            isLoading = true
            error = null
            try {
                orderList = repository.loadClientOrders(
                    token = session.token,
                    status = statusFilter.value,
                    dateFrom = dateFrom.trim().takeIf { it.isNotBlank() },
                    dateTo = dateTo.trim().takeIf { it.isNotBlank() }
                )
            } catch (e: Exception) {
                error = e.message ?: "Не удалось загрузить заказы"
            } finally {
                isLoading = false
            }
        }
    }

    fun loadDetail(orderId: Long) {
        scope.launch {
            detailLoading = true
            error = null
            try {
                selectedOrder = repository.loadClientOrderDetail(
                    token = session.token,
                    id = orderId
                )
            } catch (e: Exception) {
                error = e.message ?: "Не удалось загрузить детали заказа"
            } finally {
                detailLoading = false
            }
        }
    }

    fun reserve(orderId: Long) {
        scope.launch {
            actionLoadingId = orderId
            error = null
            try {
                repository.reserveClientOrder(
                    token = session.token,
                    id = orderId
                )
                reloadOrders()
                selectedOrder = null
            } catch (e: Exception) {
                error = e.message ?: "Не удалось перевести заказ в reserved"
            } finally {
                actionLoadingId = null
            }
        }
    }

    fun ship(orderId: Long) {
        scope.launch {
            actionLoadingId = orderId
            error = null
            try {
                repository.shipClientOrder(
                    token = session.token,
                    id = orderId
                )
                reloadOrders()
                selectedOrder = null
            } catch (e: Exception) {
                error = e.message ?: "Не удалось перевести заказ в shipped"
            } finally {
                actionLoadingId = null
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadOrders()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
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
                    Text(
                        text = "Фильтры",
                        style = MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Column(modifier = Modifier.width(180.dp)) {

                        Text(
                            text = "Статус",
                            style = MaterialTheme.typography.bodyMedium
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        StorekeeperClientStatusDropdownField(
                            value = statusFilter,
                            onValueChange = {
                                statusFilter = it
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = dateFrom,
                            onValueChange = { dateFrom = it },
                            label = { Text("Дата от (yyyy-MM-dd)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = dateTo,
                            onValueChange = { dateTo = it },
                            label = { Text("Дата до (yyyy-MM-dd)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { reloadOrders() }) {
                            Text("Применить")
                        }

                        OutlinedButton(
                            onClick = {
                                statusFilter = StorekeeperClientOrderStatus.ALL
                                dateFrom = ""
                                dateTo = ""
                                reloadOrders()
                            }
                        ) {
                            Text("Сбросить")
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        if (isLoading) {
            LinearProgressIndicatorCompat()
            Spacer(modifier = Modifier.height(12.dp))
        }

        error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        NavHost(
            navController = navController,
            startDestination = "list"
        ) {

            composable("list") {

                Card(
                    modifier = Modifier.fillMaxSize(),
                    shape = RoundedCornerShape(22.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(12.dp)
                    ) {

                        StorekeeperClientOrdersHeaderRow()

                        Spacer(modifier = Modifier.height(6.dp))

                        if (orderList.isEmpty()) {

                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "Нет заказов",
                                    style = MaterialTheme.typography.bodyMedium.copy(fontSize = 12.sp),
                                    color = Color.Gray
                                )
                            }

                        } else {

                            LazyColumn(
                                modifier = Modifier.fillMaxSize(),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {

                                itemsIndexed(orderList) { index, item ->

                                    StorekeeperClientOrderRowCard(
                                        index = index,
                                        item = item,
                                        onDetailClick = {
                                            navController.navigate("detail/${item.id}")
                                        },
                                        onReserveClick = {
                                            if (item.status == "confirmed") reserve(item.id)
                                        },
                                        onShipClick = {
                                            if (item.status == "reserved") ship(item.id)
                                        },
                                        actionLoading = actionLoadingId == item.id
                                    )
                                }
                            }
                        }
                    }
                }
            }

            composable("detail/{id}") { entry ->

                val id = entry.arguments
                    ?.getString("id")
                    ?.toLongOrNull()
                    ?: return@composable

                LaunchedEffect(id) {
                    loadDetail(id)
                }

                selectedOrder?.let {
                    StorekeeperClientOrderDetailScreen(
                        detail = it,
                        onBack = {
                            navController.popBackStack()
                        }
                    )
                }
            }
        }
    }

    if (detailLoading) {
        SimpleLoadingDialog()
    }
}

@Composable
private fun StorekeeperClientStatusDropdownField(
    value: StorekeeperClientOrderStatus,
    onValueChange: (StorekeeperClientOrderStatus) -> Unit,
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

            StorekeeperClientOrderStatus.entries.forEach { option ->

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

@Composable
private fun StorekeeperClientOrderDetailScreen(
    detail: ManagerClientOrderDetailResponse,
    onBack: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
    ) {

        Text(
            text = "Детали заказа",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp)
        ) {

            Column(modifier = Modifier.padding(16.dp)) {

                Text("Номер: ${detail.orderNumber}")
                Text("Дата: ${detail.orderDate}")
                Text("Статус: ${detail.status}")
                Text("Сумма: ${detail.totalAmount}")
                Text("Источник: ${detail.source ?: "—"}")
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Товары",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        detail.items.forEach { item ->

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp),
                shape = RoundedCornerShape(14.dp)
            ) {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Box(modifier = Modifier.weight(1.8f)) {
                        Text(item.assortmentName)
                    }

                    Box(
                        modifier = Modifier.weight(1f),
                        contentAlignment = Alignment.CenterEnd
                    ) {
                        Text("× ${item.quantity}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onBack) {
            Text("Назад")
        }
    }
}

@Composable
private fun StorekeeperClientOrdersHeaderRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE8E1FF),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        listOf("Номер", "Дата", "Статус", "Действия").forEachIndexed { index, header ->
            val weight = when (index) {
                0 -> STOREKEEPER_ORDER_TITLE_WEIGHT
                1 -> STOREKEEPER_ORDER_DATE_WEIGHT
                2 -> STOREKEEPER_ORDER_STATUS_WEIGHT
                else -> STOREKEEPER_ORDER_ACTION_WEIGHT
            }

            Box(
                modifier = Modifier.weight(weight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
                    color = Color(0xFF3E2C6B),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun StorekeeperClientOrderRowCard(
    index: Int,
    item: ClientOrderResponse,
    onDetailClick: () -> Unit,
    onReserveClick: () -> Unit,
    onShipClick: () -> Unit,
    actionLoading: Boolean
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
        colors = CardDefaults.cardColors(
            containerColor = colors[index % colors.size]
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_ORDER_TITLE_WEIGHT)
                    .padding(end = 6.dp)
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Номер заказа:\n${item.orderNumber}")
                        }
                    },
                    state = rememberTooltipState(),
                    enableUserInput = true
                ) {
                    Text(
                        text = item.orderNumber,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 8.sp,
                            lineHeight = 1.em
                        ),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_ORDER_DATE_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Дата заказа:\n${item.orderDate}")
                        }
                    },
                    state = rememberTooltipState(),
                    enableUserInput = true
                ) {
                    Text(
                        text = item.orderDate,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            lineHeight = 1.em
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_ORDER_STATUS_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Статус:\n${item.status}")
                        }
                    },
                    state = rememberTooltipState(),
                    enableUserInput = true
                ) {
                    Text(
                        text = item.status,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 11.sp,
                            lineHeight = 1.em
                        ),
                        textAlign = TextAlign.Center,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }

            Row(
                modifier = Modifier
                    .weight(STOREKEEPER_ORDER_ACTION_WEIGHT)
                    .padding(start = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                IconButton(
                    onClick = onDetailClick,
                    modifier = Modifier.width(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.Info,
                        contentDescription = "Информация"
                    )
                }

                when (item.status) {

                    "confirmed" -> {

                        IconButton(
                            onClick = onReserveClick,
                            enabled = !actionLoading,
                            modifier = Modifier.width(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.CheckCircle,
                                contentDescription = "Принять заказ"
                            )
                        }
                    }

                    "reserved" -> {

                        IconButton(
                            onClick = onShipClick,
                            enabled = !actionLoading,
                            modifier = Modifier.width(36.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Filled.LocalShipping,
                                contentDescription = "Отгрузить заказ"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorekeeperClientOrderDetailDialog(
    detail: ManagerClientOrderDetailResponse,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Заказ ${detail.orderNumber}")
        },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState())
            ) {
                Text("Дата: ${detail.orderDate}")
                Text("Статус: ${detail.status}")
                Text("Сумма: ${detail.totalAmount}")
                Text("Источник: ${detail.source ?: "—"}")

                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider()
                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Товары",
                    style = MaterialTheme.typography.titleSmall
                )

                Spacer(modifier = Modifier.height(8.dp))

                detail.items.forEach { item ->
                    Text("• ${item.assortmentName} × ${item.quantity}")
                }
            }
        },
        confirmButton = {
            OutlinedButton(onClick = onDismiss) {
                Text("Закрыть")
            }
        }
    )
}

@Composable
private fun SimpleLoadingDialog() {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("Загрузка") },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CircularProgressIndicator(modifier = Modifier.width(24.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text("Подождите...")
            }
        },
        confirmButton = {}
    )
}

@Composable
private fun LinearProgressIndicatorCompat() {
    androidx.compose.material3.LinearProgressIndicator(
        modifier = Modifier.fillMaxWidth()
    )
}