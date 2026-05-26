package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productsalessupportclient.data.network.AssortmentUpsertRequest
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.DashboardApi
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository

@Composable
fun PurchaserAssortmentEditScreen(
    session: AuthSession,
    id: Long,
    onBack: () -> Unit,
    onSaved: () -> Unit
) {
    val repository = remember {
        PurchaserDashboardRepository(
            api = DashboardApi(),
            promoApi = ClientManagerDashboardApi()
        )
    }

    val vm: PurchaserAssortmentEditViewModel = viewModel(
        factory = PurchaserAssortmentEditViewModelFactory(repository, session.token)
    )

    val state = vm.uiState

    var name by rememberSaveable { mutableStateOf("") }
    var price by rememberSaveable { mutableStateOf("") }
    var category by rememberSaveable { mutableStateOf("") }
    var article by rememberSaveable { mutableStateOf("") }
    var stockQuantity by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(id) {
        vm.load(id)
    }

    LaunchedEffect(state.item) {
        val item = state.item ?: return@LaunchedEffect
        if (name.isBlank()) {
            name = item.name
            price = item.price.toString()
            category = item.category.orEmpty()
            article = item.article.orEmpty()
            stockQuantity = (item.stockQuantity ?: 0).toString()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Изменение товара", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        OutlinedTextField(value = name, onValueChange = { name = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = price, onValueChange = { if (it.isEmpty() || it.toDoubleOrNull() != null) price = it }, label = { Text("Цена") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = category, onValueChange = { category = it }, label = { Text("Категория") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = article, onValueChange = { article = it }, label = { Text("Артикул") }, modifier = Modifier.fillMaxWidth())
        Spacer(Modifier.height(8.dp))
        OutlinedTextField(value = stockQuantity, onValueChange = { if (it.all(Char::isDigit)) stockQuantity = it }, label = { Text("Остаток") }, modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = {
                    vm.save(
                        id = id,
                        request = AssortmentUpsertRequest(
                            name = name,
                            price = price.toDoubleOrNull() ?: 0.0,
                            category = category.trim().takeIf { it.isNotBlank() },
                            article = article.trim().takeIf { it.isNotBlank() }
                        ),
                        stockQuantity = stockQuantity.toIntOrNull(),
                        onSuccess = onSaved
                    )
                }
            ) { Text("Сохранить") }

            OutlinedButton(onClick = onBack) { Text("Назад") }
        }

        if (state.isLoading) {
            Spacer(Modifier.height(12.dp))
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
        }
    }
}