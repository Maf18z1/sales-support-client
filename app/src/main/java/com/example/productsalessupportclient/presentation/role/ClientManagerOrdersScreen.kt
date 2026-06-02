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
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.productsalessupportclient.data.network.CreateManagerClientOrderItemRequest
import com.example.productsalessupportclient.data.network.CreateManagerClientOrderRequest
import com.example.productsalessupportclient.data.network.ManagerAssortmentResponse
import com.example.productsalessupportclient.data.network.ManagerClientOrderItemResponse
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.ClientManagerDashboardRepository
import kotlinx.coroutines.launch

@Composable
fun ClientManagerOrdersScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val repository = remember {
        ClientManagerDashboardRepository(
            api = com.example.productsalessupportclient.data.network.ClientManagerDashboardApi()
        )
    }

    val vm: ClientManagerOrdersViewModel = viewModel(
        factory = ClientManagerOrdersViewModelFactory(repository, session.token)
    )

    val navController = rememberNavController()
    var appliedStatusFilter by rememberSaveable { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        vm.loadOrders(null)
        vm.loadAssortment()
        vm.loadClients()
    }

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            ManagerOrdersListPage(
                state = vm.uiState,
                onReload = { statusFilter ->
                    appliedStatusFilter = statusFilter
                    vm.loadOrders(statusFilter)
                },
                onOpenCreate = {
                    navController.navigate("create")
                },
                onOpenDetail = { orderId ->
                    navController.navigate("detail/$orderId")
                },
                onConfirm = { orderId ->
                    vm.confirmOrder(
                        orderId = orderId,
                        statusFilter = appliedStatusFilter
                    )
                }
            )
        }

        composable("create") {
            ManagerOrderCreatePage(
                state = vm.uiState,
                onBack = { navController.popBackStack() },
                onCreate = { request ->
                    vm.createOrder(
                        request = request,
                        statusFilter = appliedStatusFilter
                    ) {
                        navController.popBackStack()
                    }
                }
            )
        }

        composable("detail/{orderId}") { entry ->
            val orderId = entry.arguments?.getString("orderId")?.toLongOrNull()
                ?: return@composable

            LaunchedEffect(orderId) {
                vm.loadOrderDetail(orderId)
            }

            ManagerOrderDetailPage(
                state = vm.uiState,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun ManagerOrdersListPage(
    state: ClientManagerOrdersUiState,
    onReload: (String?) -> Unit,
    onOpenCreate: () -> Unit,
    onOpenDetail: (Long) -> Unit,
    onConfirm: (Long) -> Unit
) {
    var showFilters by rememberSaveable { mutableStateOf(false) }
    var statusFilter by rememberSaveable { mutableStateOf("all") }
    var dateSort by rememberSaveable { mutableStateOf("desc") }

    val filteredOrders = remember(state.orders, statusFilter, dateSort) {
        val filtered = state.orders.filter { order ->
            statusFilter == "all" || order.status.equals(statusFilter, ignoreCase = true)
        }

        when (dateSort) {
            "asc" -> filtered.sortedBy { it.orderDate }
            else -> filtered.sortedByDescending { it.orderDate }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Заказы клиентов",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.weight(1f)
            )

            IconButton(onClick = onOpenCreate) {
                Icon(
                    imageVector = Icons.Filled.Add,
                    contentDescription = "Создать заказ"
                )
            }
        }

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
                        Column(modifier = Modifier.width(140.dp)) {
                            Text(
                                text = "Статус",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            StatusDropdownField(
                                value = statusFilter,
                                onValueChange = { statusFilter = it },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Column(modifier = Modifier.width(190.dp)) {
                            Text(
                                text = "Дата заказа",
                                style = MaterialTheme.typography.bodyMedium
                            )

                            Spacer(modifier = Modifier.height(6.dp))

                            Column(
                                verticalArrangement = Arrangement.spacedBy(0.dp)
                            ) {

                                FilterChip(
                                    selected = dateSort == "asc",
                                    onClick = { dateSort = "asc" },
                                    label = { Text("По возрастанию") }
                                )

                                FilterChip(
                                    selected = dateSort == "desc",
                                    onClick = { dateSort = "desc" },
                                    label = { Text("По убыванию") }
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = {
                                onReload(statusFilter.takeIf { it != "all" })
                            }
                        ) {
                            Text("Применить")
                        }

                        OutlinedButton(
                            onClick = {
                                statusFilter = "all"
                                dateSort = "desc"
                                onReload(null)
                            }
                        ) {
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

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredOrders, key = { it.id }) { item ->
                ManagerOrderRowCard(
                    item = item,
                    onInfoClick = { onOpenDetail(item.id) },
                    onConfirmClick = { onConfirm(item.id) }
                )
            }
        }
    }
}

@Composable
private fun ManagerOrderDetailPage(
    state: ClientManagerOrdersUiState,
    onBack: () -> Unit
) {
    val detail = state.orderDetail

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = "Детальная информация",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        if (detail == null) {
            Text("Нет данных по заказу")
        } else {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(22.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(detail.orderNumber, style = MaterialTheme.typography.titleLarge)
                    Spacer(modifier = Modifier.height(6.dp))
                    Text("Дата: ${detail.orderDate}")
                    Text("Статус: ${detail.status}")
                    Text("Источник: ${detail.source ?: "—"}")
                    Text("Сумма: ${detail.totalAmount}")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Состав заказа",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                detail.items.forEach { item ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = item.assortmentName,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text("Количество: ${item.quantity}")
                        }
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
private fun ManagerOrderCreatePage(
    state: ClientManagerOrdersUiState,
    onBack: () -> Unit,
    onCreate: (CreateManagerClientOrderRequest) -> Unit
) {
    var selectedClientId by rememberSaveable { mutableStateOf<Long?>(null) }

    val selectedClient = state.clients.firstOrNull { it.id == selectedClientId }
        ?: state.clients.firstOrNull()

    LaunchedEffect(state.clients) {
        if (selectedClientId == null && state.clients.isNotEmpty()) {
            selectedClientId = state.clients.first().id
        }
    }

    var lines by remember {
        mutableStateOf(listOf(ManagerOrderCreateLineUiState()))
    }

    val totalAmount by remember(lines, state.assortment) {
        derivedStateOf {
            lines.sumOf { line ->
                val product = state.assortment.firstOrNull { it.id == line.assortmentId }
                val qty = line.quantity.toIntOrNull() ?: 0
                (product?.price ?: 0.0) * qty
            }
        }
    }

    val validItems = remember(lines) {
        lines.mapNotNull { line ->
            val assortmentId = line.assortmentId ?: return@mapNotNull null
            val quantity = line.quantity.toIntOrNull()?.takeIf { it > 0 } ?: return@mapNotNull null
            CreateManagerClientOrderItemRequest(
                assortmentId = assortmentId,
                quantity = quantity
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text(
            text = "Создание заказа",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.error?.let {
            Text(text = it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(12.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                Text("Клиент", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                ClientDropdownField(
                    value = selectedClient?.fullName ?: "Клиенты не загружены",
                    options = state.clients,
                    onValueChange = { selectedClientId = it.id }
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text("Товары в заказе", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))

                state.assortment.forEach { }
                lines.forEachIndexed { index, line ->
                    ManagerOrderLineEditor(
                        line = line,
                        assortment = state.assortment,
                        onChange = { updatedLine ->
                            lines = lines.toMutableList().also { it[index] = updatedLine }
                        },
                        onRemove = {
                            if (lines.size > 1) {
                                lines = lines.toMutableList().also { it.removeAt(index) }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(8.dp))
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = {
                            lines = lines + ManagerOrderCreateLineUiState()
                        }
                    ) {
                        Text("Добавить товар")
                    }

                    TextButton(
                        onClick = {
                            lines = listOf(ManagerOrderCreateLineUiState())
                        }
                    ) {
                        Text("Очистить")
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Сумма заказа: $totalAmount",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = buildString {
                                appendLine("Клиент выбран:")
                                appendLine(selectedClient?.fullName ?: "—")
                                appendLine(selectedClient?.phone ?: "—")
                                appendLine(selectedClient?.email ?: "—")
                                append(selectedClient?.address ?: "—")
                            },
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        val clientId = selectedClientId ?: return@Button
                        if (validItems.isNotEmpty()) {
                            onCreate(
                                CreateManagerClientOrderRequest(
                                    clientId = clientId,
                                    items = validItems,
                                    source = "manual"
                                )
                            )
                        }
                    },
                    enabled = validItems.isNotEmpty() && selectedClientId != null
                ) {
                    Text("Создать заказ")
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Заказ сохранится сразу со статусом confirmed.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(onClick = onBack) {
            Text("Назад")
        }
    }
}

@Composable
private fun ManagerOrderRowCard(
    item: ClientOrderResponse,
    onInfoClick: () -> Unit,
    onConfirmClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text("Дата: ${item.orderDate}")
                Text("Статус: ${item.status}")
                Text("Сумма: ${item.totalAmount}")
            }

            IconButton(onClick = onInfoClick) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = "Подробная информация"
                )
            }

            if (item.status.equals("new", ignoreCase = true)) {
                IconButton(onClick = onConfirmClick) {
                    Icon(
                        imageVector = Icons.Filled.CheckCircle,
                        contentDescription = "Подтвердить"
                    )
                }
            }
        }
    }
}

@Composable
private fun StatusDropdownField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val options = remember {
        listOf("all", "new", "confirmed", "reserved", "shipped", "cancelled")
    }

    Box(modifier = modifier) {
        OutlinedButton(onClick = { expanded = true }, modifier = Modifier.fillMaxWidth()) {
            Text(value.uppercase())
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.uppercase()) },
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
private fun ClientDropdownField(
    value: String,
    options: List<com.example.productsalessupportclient.data.network.ClientResponse>,
    onValueChange: (com.example.productsalessupportclient.data.network.ClientResponse) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        OutlinedButton(onClick = { expanded = true }) {
            Text(value, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.fullName) },
                    onClick = {
                        expanded = false
                        onValueChange(option)
                    }
                )
            }
        }
    }
}

data class ManagerOrderCreateLineUiState(
    val assortmentId: Long? = null,
    val quantity: String = "1"
)

@Composable
private fun ManagerOrderLineEditor(
    line: ManagerOrderCreateLineUiState,
    assortment: List<ManagerAssortmentResponse>,
    onChange: (ManagerOrderCreateLineUiState) -> Unit,
    onRemove: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProductDropdownField(
            products = assortment,
            selectedId = line.assortmentId,
            onSelected = { id ->
                onChange(line.copy(assortmentId = id))
            },
            modifier = Modifier.weight(1f)
        )

        OutlinedTextField(
            value = line.quantity,
            onValueChange = {
                if (it.all(Char::isDigit)) {
                    onChange(line.copy(quantity = it))
                }
            },
            label = { Text("Кол-во") },
            singleLine = true,
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.width(90.dp)
        )

        IconButton(onClick = onRemove) {
            Text("×")
        }
    }
}

@Composable
private fun ProductDropdownField(
    products: List<ManagerAssortmentResponse>,
    selectedId: Long?,
    onSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedText = remember(products, selectedId) {
        products.firstOrNull { it.id == selectedId }?.let {
            "${it.name} (${it.price})"
        } ?: "Выберите товар"
    }

    Box(modifier = modifier) {
        OutlinedButton(
            onClick = { expanded = true },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = selectedText,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Start
            )
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            products.forEach { product ->
                DropdownMenuItem(
                    text = {
                        Text("${product.name} • ${product.price}")
                    },
                    onClick = {
                        expanded = false
                        onSelected(product.id)
                    }
                )
            }
        }
    }
}