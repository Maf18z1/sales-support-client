package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.CreateSupplierOrderRequest
import com.example.productsalessupportclient.data.network.SupplierCatalogItemResponse
import com.example.productsalessupportclient.data.network.SupplierOrderItemRequest
import com.example.productsalessupportclient.data.network.SupplierResponse
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import com.example.productsalessupportclient.data.network.DashboardApi

@Composable
fun PurchaserSupplierOrdersScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val repository = remember {
        PurchaserDashboardRepository(DashboardApi(),ClientManagerDashboardApi())
    }

    val vm: PurchaserSupplierOrdersViewModel = viewModel(
        factory = PurchaserSupplierOrdersViewModelFactory(repository, session.token)
    )

    val state = vm.uiState
    val navController = rememberNavController()

    var showFilters by rememberSaveable { mutableStateOf(false) }
    var statusFilter by rememberSaveable { mutableStateOf("") }
    var selectedSupplierId by rememberSaveable { mutableStateOf<Long?>(null) }
    var dateFrom by rememberSaveable { mutableStateOf("") }
    var dateTo by rememberSaveable { mutableStateOf("") }

    val reloadCurrentFilters = {
        vm.loadOrders(
            status = statusFilter.trim().takeIf { it.isNotBlank() },
            supplierId = selectedSupplierId,
            dateFrom = dateFrom.trim().takeIf { it.isNotBlank() },
            dateTo = dateTo.trim().takeIf { it.isNotBlank() }
        )
    }

    NavHost(
        navController = navController,
        startDestination = "list"
    ) {
        composable("list") {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
                    .padding(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Заказы поставщикам",
                        style = MaterialTheme.typography.headlineSmall
                    )

                    IconButton(onClick = { navController.navigate("create") }) {
                        Icon(Icons.Filled.Add, contentDescription = "Создать заказ")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

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
                        Column(
                            modifier = Modifier.padding(12.dp)
                        ) {
                            Text("Фильтры", style = MaterialTheme.typography.titleMedium)
                            Spacer(modifier = Modifier.height(10.dp))

                            OutlinedTextField(
                                value = statusFilter,
                                onValueChange = { statusFilter = it },
                                label = { Text("Статус") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("new / sent / in_transit / received") }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            SupplierDropdown(
                                suppliers = state.suppliers,
                                selectedSupplierId = selectedSupplierId,
                                onSelected = { selectedSupplierId = it }
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = dateFrom,
                                    onValueChange = { dateFrom = it },
                                    label = { Text("Дата от") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("2026-05-01") }
                                )
                                OutlinedTextField(
                                    value = dateTo,
                                    onValueChange = { dateTo = it },
                                    label = { Text("Дата до") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    placeholder = { Text("2026-05-31") }
                                )
                            }

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { reloadCurrentFilters() }) {
                                    Text("Применить")
                                }

                                OutlinedButton(
                                    onClick = {
                                        statusFilter = ""
                                        selectedSupplierId = null
                                        dateFrom = ""
                                        dateTo = ""
                                        vm.loadOrders(null, null, null, null)
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
                    items(state.orders) { order ->
                        SupplierOrderCard(
                            orderNumber = order.orderNumber,
                            status = order.status,
                            orderDate = order.orderDate,
                            supplierName = order.supplierName,
                            onInfoClick = { navController.navigate("info/${order.id}") }
                        )
                    }
                }
            }
        }

        composable("create") {
            PurchaserSupplierOrderCreateScreen(
                session = session,
                onBack = { navController.popBackStack() },
                onCreated = {
                    reloadCurrentFilters()
                    navController.popBackStack()
                }
            )
        }

        composable("info/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull()
                ?: return@composable

            PurchaserSupplierOrderInfoScreen(
                session = session,
                id = id,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun SupplierOrderCard(
    orderNumber: String,
    status: String,
    orderDate: String,
    supplierName: String,
    onInfoClick: () -> Unit
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
                    text = orderNumber,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Статус: $status",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Дата: $orderDate",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Поставщик: $supplierName",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            IconButton(onClick = onInfoClick) {
                Icon(Icons.Filled.Info, contentDescription = "Подробнее")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierDropdown(
    suppliers: List<SupplierResponse>,
    selectedSupplierId: Long?,
    onSelected: (Long?) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedName = suppliers.firstOrNull { it.id == selectedSupplierId }?.name ?: "Все поставщики"

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded }
    ) {
        OutlinedTextField(
            value = selectedName,
            onValueChange = {},
            readOnly = true,
            label = { Text("Поставщик") },
            singleLine = true,
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("Все поставщики") },
                onClick = {
                    onSelected(null)
                    expanded = false
                }
            )

            suppliers.forEach { supplier ->
                DropdownMenuItem(
                    text = { Text(supplier.name) },
                    onClick = {
                        onSelected(supplier.id)
                        expanded = false
                    }
                )
            }
        }
    }
}