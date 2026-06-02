package com.example.productsalessupportclient.presentation.role

import android.os.Build
import androidx.annotation.RequiresApi
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productsalessupportclient.data.network.AssortmentWithStockResponse
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.LaunchedEffect
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PurchaserAssortmentScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val repository = remember {
        PurchaserDashboardRepository(
            api = com.example.productsalessupportclient.data.network.DashboardApi(),
            promoApi = ClientManagerDashboardApi()
        )
    }

    val vm: PurchaserAssortmentViewModel = viewModel(
        factory = PurchaserAssortmentViewModelFactory(repository, session.token)
    )

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var minStock by rememberSaveable { mutableStateOf("") }
    var maxStock by rememberSaveable { mutableStateOf("") }
    var expiringDays by rememberSaveable { mutableStateOf("") }

    val state = vm.uiState

    LaunchedEffect(Unit) {
        vm.load(
            category = null,
            minStock = null,
            maxStock = null,
            expiringDays = null
        )
    }

    val navController = rememberNavController()
    var pendingDeleteId by rememberSaveable { mutableStateOf<Long?>(null) }

    var showFilters by rememberSaveable { mutableStateOf(false) }

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
            androidx.compose.material3.OutlinedButton(
                onClick = { showFilters = !showFilters }
            ) {
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
                        value = searchQuery,
                        onValueChange = { searchQuery = it },
                        label = { Text("Поиск по названию ассортимента") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = category,
                            onValueChange = { category = it },
                            label = { Text("Категория") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = expiringDays,
                            onValueChange = { if (it.all(Char::isDigit)) expiringDays = it },
                            label = { Text("Срок") },
                            singleLine = true,
                            modifier = Modifier.width(110.dp),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = minStock,
                            onValueChange = { if (it.all(Char::isDigit)) minStock = it },
                            label = { Text("Остаток от") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                        OutlinedTextField(
                            value = maxStock,
                            onValueChange = { if (it.all(Char::isDigit)) maxStock = it },
                            label = { Text("Остаток до") },
                            singleLine = true,
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = androidx.compose.ui.text.input.KeyboardType.Number
                            )
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        androidx.compose.material3.Button(
                            onClick = {
                                vm.load(
                                    category = category.trim().takeIf { it.isNotBlank() },
                                    minStock = minStock.toIntOrNull(),
                                    maxStock = maxStock.toIntOrNull(),
                                    expiringDays = expiringDays.toIntOrNull()
                                )
                            }
                        ) {
                            Text("Применить")
                        }

                        androidx.compose.material3.OutlinedButton(
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

        val reloadCurrentFilters = {
            vm.load(
                category = category.trim().takeIf { it.isNotBlank() },
                minStock = minStock.toIntOrNull(),
                maxStock = maxStock.toIntOrNull(),
                expiringDays = expiringDays.toIntOrNull()
            )
        }

        val filteredItems = state.items.filter { item ->
            searchQuery.isBlank() ||
                    item.name.contains(searchQuery, ignoreCase = true)
        }

        NavHost(navController = navController, startDestination = "list") {
            composable("list") {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredItems) { item ->
                        AssortmentRowCard(
                            item = item,
                            onEditClick = { navController.navigate("edit/${item.id}") },
                            onDeleteClick = { pendingDeleteId = item.id },
                            onInfoClick = { navController.navigate("info/${item.id}") }
                        )
                    }
                }
            }

            composable("edit/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                PurchaserAssortmentEditScreen(
                    session = session,
                    id = id,
                    onBack = { navController.popBackStack() },
                    onSaved = {
                        reloadCurrentFilters()
                        navController.popBackStack()
                    }
                )
            }

            composable("info/{id}") { entry ->
                val id = entry.arguments?.getString("id")?.toLongOrNull() ?: return@composable
                PurchaserAssortmentInfoScreen(
                    session = session,
                    id = id,
                    onBack = { navController.popBackStack() }
                )
            }
        }

        if (pendingDeleteId != null) {
            androidx.compose.material3.AlertDialog(
                onDismissRequest = { pendingDeleteId = null },
                title = { Text("Удалить товар?") },
                text = { Text("Товар будет удалён только из ассортимента.") },
                confirmButton = {
                    androidx.compose.material3.TextButton(
                        onClick = {
                            val id = pendingDeleteId ?: return@TextButton
                            pendingDeleteId = null
                            vm.deleteAssortment(id) {
                                reloadCurrentFilters()
                            }
                        }
                    ) {
                        Text("Удалить")
                    }
                },
                dismissButton = {
                    androidx.compose.material3.TextButton(
                        onClick = { pendingDeleteId = null }
                    ) {
                        Text("Отмена")
                    }
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AssortmentRowCard(
    item: AssortmentWithStockResponse,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
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
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Категория: ${item.category ?: "—"}",
                    style = MaterialTheme.typography.bodySmall
                )
                Text(
                    text = "Остаток: ${item.stockQuantity ?: 0}",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Filled.Edit, contentDescription = "Изменить")
                }

                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Filled.Delete, contentDescription = "Удалить")
                }

                IconButton(onClick = onInfoClick) {
                    Icon(Icons.Filled.Info, contentDescription = "Инфо")
                }
            }
        }
    }
}