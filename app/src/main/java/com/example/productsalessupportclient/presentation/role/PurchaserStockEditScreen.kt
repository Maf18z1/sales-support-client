package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productsalessupportclient.data.network.StockBatchUpsertRequest
import com.example.productsalessupportclient.data.network.StockDetailUpsertRequest
import com.example.productsalessupportclient.data.network.StockApi
import com.example.productsalessupportclient.data.network.ReportApi
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.PurchaserStockRepository

@Composable
fun PurchaserStockEditScreen(
    session: AuthSession,
    assortmentId: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val repository = remember {
        PurchaserStockRepository(StockApi(), ReportApi())
    }

    val vm: PurchaserStockEditViewModel = viewModel(
        factory = PurchaserStockEditViewModelFactory(repository, session.token)
    )

    val state = vm.uiState

    var name by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var article by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var stockQuantity by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(assortmentId) {
        vm.load(assortmentId)
    }

    LaunchedEffect(state.detail) {
        state.detail?.let { d ->
            name = d.name
            category = d.category.orEmpty()
            article = d.article.orEmpty()
            price = d.price.toString()
            stockQuantity = d.stockQuantity.toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(12.dp)
    ) {
        Text("Редактирование остатков", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(12.dp))

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(modifier = Modifier.height(8.dp))
        }

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(modifier = Modifier.padding(12.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Название") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    label = { Text("Категория") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = article,
                    onValueChange = { article = it },
                    label = { Text("Артикул") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = price,
                    onValueChange = { if (it.all { ch -> ch.isDigit() || ch == '.' }) price = it },
                    label = { Text("Цена") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = stockQuantity,
                    onValueChange = { if (it.all(Char::isDigit)) stockQuantity = it },
                    label = { Text("Количество на складе") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Button(
                    onClick = {
                        vm.saveDetail(
                            assortmentId = assortmentId,
                            request = StockDetailUpsertRequest(
                                name = name,
                                category = category.trim().takeIf { it.isNotBlank() },
                                article = article.trim().takeIf { it.isNotBlank() },
                                price = price.toDoubleOrNull() ?: 0.0,
                                stockQuantity = stockQuantity.toIntOrNull() ?: 0
                            ),
                            onSaved = onSaved
                        )
                    }
                ) {
                    Text("Сохранить")
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Text("Партии", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            state.batches.forEach { batch ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Партия #${batch.batchId}",
                            style = MaterialTheme.typography.titleSmall
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Количество: ${batch.quantity}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Срок годности: ${batch.expiryDate}",
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text(
                            text = "Дата получения: ${batch.receivedDate}",
                            style = MaterialTheme.typography.bodyMedium
                        )
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
private fun BatchEditorCard(
    batchId: Long,
    quantity: Int,
    expiryDate: String,
    receivedDate: String,
    onSave: (Int, String, String?) -> Unit,
    onDelete: () -> Unit
) {
    var qty by rememberSaveable(batchId) { mutableStateOf(quantity.toString()) }
    var expiry by rememberSaveable(batchId) { mutableStateOf(expiryDate) }
    var received by rememberSaveable(batchId) { mutableStateOf(receivedDate) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text("Партия #$batchId", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = qty,
                onValueChange = { if (it.all(Char::isDigit)) qty = it },
                label = { Text("Количество") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = expiry,
                onValueChange = { expiry = it },
                label = { Text("Срок годности") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
            OutlinedTextField(
                value = received,
                onValueChange = { received = it },
                label = { Text("Дата поступления") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Button(
                    onClick = {
                        onSave(qty.toIntOrNull() ?: quantity, expiry, received)
                    }
                ) {
                    Text("Сохранить")
                }

                OutlinedButton(onClick = onDelete) {
                    Text("Удалить")
                }
            }
        }
    }
}