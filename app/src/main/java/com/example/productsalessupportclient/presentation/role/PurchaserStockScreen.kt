package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ShowChart
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
import com.example.productsalessupportclient.data.network.StockOverviewResponse
import com.example.productsalessupportclient.data.network.StockApi
import com.example.productsalessupportclient.data.network.ReportApi
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.PurchaserStockRepository

@Composable
fun PurchaserStockScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val repository = remember {
        PurchaserStockRepository(StockApi(), ReportApi())
    }

    val vm: PurchaserStockViewModel = viewModel(
        factory = PurchaserStockViewModelFactory(repository, session.token)
    )

    val state = vm.uiState
    val navController = rememberNavController()

    var showFilters by rememberSaveable { mutableStateOf(false) }
    var category by rememberSaveable { mutableStateOf("") }
    var minStock by rememberSaveable { mutableStateOf("") }
    var maxStock by rememberSaveable { mutableStateOf("") }
    var expiringDays by rememberSaveable { mutableStateOf("") }

    val reloadCurrentFilters = {
        vm.load(
            category = category.trim().takeIf { it.isNotBlank() },
            minStock = minStock.toIntOrNull(),
            maxStock = maxStock.toIntOrNull(),
            expiringDays = expiringDays.toIntOrNull()
        )
    }

    LaunchedEffect(Unit) {
        vm.load(null, null, null, null)
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
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Отслеживание остатков",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )

                    IconButton(onClick = { navController.navigate("analytics") }) {
                        Icon(Icons.Filled.ShowChart, contentDescription = "График")
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

                            OutlinedTextField(
                                value = category,
                                onValueChange = { category = it },
                                label = { Text("Категория") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = minStock,
                                    onValueChange = { if (it.all(Char::isDigit)) minStock = it },
                                    label = { Text("Остаток от") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                                OutlinedTextField(
                                    value = maxStock,
                                    onValueChange = { if (it.all(Char::isDigit)) maxStock = it },
                                    label = { Text("Остаток до") },
                                    singleLine = true,
                                    modifier = Modifier.weight(1f),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            OutlinedTextField(
                                value = expiringDays,
                                onValueChange = { if (it.all(Char::isDigit)) expiringDays = it },
                                label = { Text("Срок, дни") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth(),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )

                            Spacer(modifier = Modifier.height(12.dp))

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                Button(onClick = { reloadCurrentFilters() }) {
                                    Text("Применить")
                                }

                                OutlinedButton(
                                    onClick = {
                                        category = ""
                                        minStock = ""
                                        maxStock = ""
                                        expiringDays = ""
                                        vm.load(null, null, null, null)
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
                    items(state.items) { item ->
                        StockOverviewCard(
                            item = item,
                            onEditClick = { navController.navigate("edit/${item.assortmentId}") }
                        )
                    }
                }
            }
        }

        composable("edit/{id}") { entry ->
            val id = entry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
            PurchaserStockEditScreen(
                session = session,
                assortmentId = id,
                onBack = { navController.popBackStack() },
                onSaved = {
                    reloadCurrentFilters()
                    navController.popBackStack()
                }
            )
        }

        composable("analytics") {
            PurchaserStockAnalyticsScreen(
                session = session,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

@Composable
private fun StockOverviewCard(
    item: StockOverviewResponse,
    onEditClick: () -> Unit
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
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Text("Категория: ${item.category ?: "—"}", style = MaterialTheme.typography.bodySmall)
                Text("Остаток: ${item.stockQuantity}", style = MaterialTheme.typography.bodySmall)
                Text("Обновлено: ${item.lastUpdated}", style = MaterialTheme.typography.bodySmall)
                Text("Срок: ${item.nearestExpiryDate ?: "—"}", style = MaterialTheme.typography.bodySmall)
            }

            IconButton(onClick = onEditClick) {
                Icon(Icons.Filled.Edit, contentDescription = "Изменить")
            }
        }
    }
}