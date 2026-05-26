package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.CreateSupplierOrderRequest
import com.example.productsalessupportclient.data.network.DashboardApi
import com.example.productsalessupportclient.data.network.SupplierOrderItemRequest
import com.example.productsalessupportclient.data.network.SupplierResponse
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository

@Composable
fun PurchaserSupplierOrderCreateScreen(
    session: AuthSession,
    onBack: () -> Unit,
    onCreated: (Long) -> Unit
) {
    val repository = remember {
        PurchaserDashboardRepository(DashboardApi(),ClientManagerDashboardApi())
    }

    val vm: PurchaserSupplierOrderCreateViewModel = viewModel(
        factory = PurchaserSupplierOrderCreateViewModelFactory(repository, session.token)
    )

    val state = vm.uiState

    var selectedSupplierId by rememberSaveable { mutableStateOf<Long?>(null) }
    var quantityInputs by remember { mutableStateOf<Map<Long, String>>(emptyMap()) }

    LaunchedEffect(Unit) {
        vm.loadSuppliers()
    }

    LaunchedEffect(state.suppliers) {
        if (selectedSupplierId == null && state.suppliers.isNotEmpty()) {
            selectedSupplierId = state.suppliers.first().id
            vm.loadProducts(state.suppliers.first().id)
        }
    }

    LaunchedEffect(state.suppliers) {

        if (
            selectedSupplierId == null &&
            state.suppliers.isNotEmpty()
        ) {

            val firstSupplierId =
                state.suppliers.first().id

            selectedSupplierId = firstSupplierId

            vm.loadProducts(firstSupplierId)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(12.dp)
    ) {
        Text(
            text = "Создание заказа поставщику",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(12.dp))

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        SupplierDropdown(
            suppliers = state.suppliers,
            selectedSupplierId = selectedSupplierId,
            onSelected = { supplierId ->
                selectedSupplierId = supplierId
                vm.loadProducts(supplierId)
            }
        )

        Spacer(modifier = Modifier.height(12.dp))

        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        Text(
            text = "Товары поставщика",
            style = MaterialTheme.typography.titleMedium
        )

        Spacer(modifier = Modifier.height(8.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(state.products) { product ->
                val currentQty = quantityInputs[product.assortmentId].orEmpty()

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = product.name,
                            style = MaterialTheme.typography.titleMedium
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "Категория: ${product.category ?: "—"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Артикул: ${product.article ?: "—"}",
                            style = MaterialTheme.typography.bodySmall
                        )
                        Text(
                            text = "Остаток: ${product.stockQuantity ?: 0}",
                            style = MaterialTheme.typography.bodySmall
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        OutlinedTextField(
                            value = currentQty,
                            onValueChange = { newValue ->
                                if (newValue.all(Char::isDigit)) {
                                    quantityInputs = quantityInputs + (product.assortmentId to newValue)
                                }
                            },
                            label = { Text("Количество") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(
                                keyboardType = KeyboardType.Number
                            )
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    val supplierId = selectedSupplierId ?: return@Button

                    val items = state.products.mapNotNull { product ->
                        val qty = quantityInputs[product.assortmentId]?.toIntOrNull() ?: 0
                        if (qty > 0) {
                            SupplierOrderItemRequest(
                                assortmentId = product.assortmentId,
                                quantity = qty
                            )
                        } else null
                    }

                    if (items.isEmpty()) return@Button

                    vm.createOrder(
                        request = CreateSupplierOrderRequest(
                            supplierId = supplierId,
                            items = items
                        ),
                        onSuccess = { createdId ->
                            onCreated(createdId)
                        }
                    )
                }
            ) {
                Text("Создать")
            }

            OutlinedButton(onClick = onBack) {
                Text("Назад")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SupplierDropdown(
    suppliers: List<SupplierResponse>,
    selectedSupplierId: Long?,
    onSelected: (Long) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    val selectedSupplier =
        suppliers.firstOrNull { it.id == selectedSupplierId }

    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = {
            expanded = !expanded
        }
    ) {

        OutlinedTextField(
            value = selectedSupplier?.name ?: "",
            onValueChange = {},
            readOnly = true,
            label = { Text("Поставщик") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded)
            },
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
        )

        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {

            suppliers.forEach { supplier ->

                DropdownMenuItem(
                    text = {
                        Text(supplier.name)
                    },
                    onClick = {
                        expanded = false
                        onSelected(supplier.id)
                    }
                )
            }
        }
    }
}