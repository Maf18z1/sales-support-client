package com.example.productsalessupportclient.presentation.role

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
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
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.network.StorekeeperDashboardApi
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.StorekeeperDashboardRepository
import kotlinx.coroutines.launch

private const val STOREKEEPER_RETURNS_TITLE_WEIGHT = 2.2f
private const val STOREKEEPER_RETURNS_DATE_WEIGHT = 1.3f
private const val STOREKEEPER_RETURNS_STATUS_WEIGHT = 1.2f
private const val STOREKEEPER_RETURNS_ACTION_WEIGHT = 1.8f

private enum class StorekeeperReturnsMode {
    ALL,
    ORDERS,
    WRITEOFFS
}

@Composable
fun StorekeeperReturnsScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val repository = remember {
        StorekeeperDashboardRepository(
            StorekeeperDashboardApi()
        )
    }

    val scope = rememberCoroutineScope()

    var isLoading by rememberSaveable { mutableStateOf(false) }
    var error by rememberSaveable { mutableStateOf<String?>(null) }

    var clientOrders by rememberSaveable { mutableStateOf(listOf<ClientOrderResponse>()) }
    var expiredBatches by rememberSaveable { mutableStateOf(listOf<ProductBatchResponse>()) }

    var mode by rememberSaveable { mutableStateOf(StorekeeperReturnsMode.ALL) }
    var showFilters by rememberSaveable { mutableStateOf(false) }

    var selectedCancelId by rememberSaveable { mutableStateOf<Long?>(null) }
    var cancelReason by rememberSaveable { mutableStateOf("") }
    var actionLoadingId by rememberSaveable { mutableStateOf<Long?>(null) }

    fun reloadData() {
        scope.launch {
            isLoading = true
            error = null
            try {
                clientOrders = repository.loadClientOrders(
                    token = session.token,
                    status = null,
                    dateFrom = null,
                    dateTo = null
                ).filter { it.status == "shipped" || it.status == "reserved" }

                expiredBatches = repository.loadExpiredBatches(session.token)
            } catch (e: Exception) {
                error = e.message ?: "Не удалось загрузить данные"
            } finally {
                isLoading = false
            }
        }
    }

    fun cancelOrder(orderId: Long) {
        scope.launch {
            actionLoadingId = orderId
            error = null
            try {
                repository.cancelClientOrder(
                    token = session.token,
                    orderId = orderId,
                    reason = cancelReason.trim().takeIf { it.isNotBlank() }
                )
                selectedCancelId = null
                cancelReason = ""
                reloadData()
            } catch (e: Exception) {
                error = e.message ?: "Не удалось отменить заказ"
            } finally {
                actionLoadingId = null
            }
        }
    }

    fun writeOffBatch(batchId: Long) {
        scope.launch {
            isLoading = true
            error = null
            try {
                repository.deleteBatch(session.token, batchId)
                reloadData()
            } catch (e: Exception) {
                error = e.message ?: "Не удалось списать партию"
            } finally {
                isLoading = false
            }
        }
    }

    LaunchedEffect(Unit) {
        reloadData()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Unspecified)
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            OutlinedButton(onClick = { showFilters = !showFilters }) {
                Text(if (showFilters) "Скрыть фильтр" else "Показать фильтр")
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
                        text = "Фильтр",
                        style = androidx.compose.material3.MaterialTheme.typography.titleMedium
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                        SegmentedButton(
                            selected = mode == StorekeeperReturnsMode.ALL,
                            onClick = { mode = StorekeeperReturnsMode.ALL },
                            shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3)
                        ) { Text("Все") }

                        SegmentedButton(
                            selected = mode == StorekeeperReturnsMode.ORDERS,
                            onClick = { mode = StorekeeperReturnsMode.ORDERS },
                            shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3)
                        ) { Text("Заказы") }

                        SegmentedButton(
                            selected = mode == StorekeeperReturnsMode.WRITEOFFS,
                            onClick = { mode = StorekeeperReturnsMode.WRITEOFFS },
                            shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3)
                        ) { Text("Списания") }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(onClick = { reloadData() }) {
                            Text("Обновить")
                        }

                        OutlinedButton(
                            onClick = {
                                mode = StorekeeperReturnsMode.ALL
                                reloadData()
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
            androidx.compose.material3.LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        error?.let {
            Text(
                text = it,
                color = androidx.compose.material3.MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        when (mode) {
            StorekeeperReturnsMode.ALL -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                ) {
                    StorekeeperReturnsOrdersBlock(
                        title = "Заказы для возврата",
                        items = clientOrders,
                        actionLoadingId = actionLoadingId,
                        onCancel = { selectedCancelId = it }
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    StorekeeperReturnsBatchesBlock(
                        title = "Товары к списанию",
                        items = expiredBatches,
                        onWriteOff = { batchId -> writeOffBatch(batchId) }
                    )
                }
            }

            StorekeeperReturnsMode.ORDERS -> {
                StorekeeperReturnsOrdersBlock(
                    title = "Заказы для возврата",
                    items = clientOrders,
                    actionLoadingId = actionLoadingId,
                    onCancel = { selectedCancelId = it }
                )
            }

            StorekeeperReturnsMode.WRITEOFFS -> {
                StorekeeperReturnsBatchesBlock(
                    title = "Товары к списанию",
                    items = expiredBatches,
                    onWriteOff = { batchId -> writeOffBatch(batchId) }
                )
            }
        }
    }

    if (selectedCancelId != null) {
        AlertDialog(
            onDismissRequest = {
                selectedCancelId = null
                cancelReason = ""
            },
            title = { Text("Отменить заказ?") },
            text = {
                Column {
                    Text("Заказ будет переведён в cancelled. Возврат будет создан на сервере.")
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = cancelReason,
                        onValueChange = { cancelReason = it },
                        label = { Text("Причина возврата") },
                        singleLine = false,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val id = selectedCancelId ?: return@Button
                        cancelOrder(id)
                    }
                ) {
                    Text("Подтвердить")
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = {
                        selectedCancelId = null
                        cancelReason = ""
                    }
                ) {
                    Text("Отмена")
                }
            }
        )
    }
}

@Composable
private fun StorekeeperReturnsOrdersBlock(
    title: String,
    items: List<ClientOrderResponse>,
    actionLoadingId: Long?,
    onCancel: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            StorekeeperReturnsHeaderRow()

            Spacer(modifier = Modifier.height(6.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет заказов",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        StorekeeperReturnsOrderRowCard(
                            index = index,
                            item = item,
                            actionLoading = actionLoadingId == item.id,
                            onCancel = { onCancel(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorekeeperReturnsBatchesBlock(
    title: String,
    items: List<ProductBatchResponse>,
    onWriteOff: (Long) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(
                text = title,
                style = androidx.compose.material3.MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            StorekeeperReturnsHeaderRow()

            Spacer(modifier = Modifier.height(6.dp))

            if (items.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Нет списаний",
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(320.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    itemsIndexed(items) { index, item ->
                        StorekeeperReturnsBatchRowCard(
                            index = index,
                            item = item,
                            onWriteOff = { onWriteOff(item.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StorekeeperReturnsHeaderRow() {
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
                0 -> STOREKEEPER_RETURNS_TITLE_WEIGHT
                1 -> STOREKEEPER_RETURNS_DATE_WEIGHT
                2 -> STOREKEEPER_RETURNS_STATUS_WEIGHT
                else -> STOREKEEPER_RETURNS_ACTION_WEIGHT
            }

            Box(
                modifier = Modifier.weight(weight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = header,
                    style = androidx.compose.material3.MaterialTheme.typography.labelMedium.copy(fontSize = 11.sp),
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
private fun StorekeeperReturnsOrderRowCard(
    index: Int,
    item: ClientOrderResponse,
    actionLoading: Boolean,
    onCancel: () -> Unit
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
                    .weight(STOREKEEPER_RETURNS_TITLE_WEIGHT)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = item.orderNumber,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 1.em
                    ),
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_RETURNS_DATE_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.orderDate,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_RETURNS_STATUS_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.status,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_RETURNS_ACTION_WEIGHT)
                    .padding(start = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(
                    onClick = onCancel,
                    enabled = !actionLoading
                ) {
                    Icon(
                        imageVector = Icons.Filled.Close,
                        contentDescription = "Отменить"
                    )
                }
            }
        }
    }
}

@Composable
private fun StorekeeperReturnsBatchRowCard(
    index: Int,
    item: ProductBatchResponse,
    onWriteOff: () -> Unit
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
                    .weight(STOREKEEPER_RETURNS_TITLE_WEIGHT)
                    .padding(end = 6.dp)
            ) {
                Text(
                    text = item.assortmentName,
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 12.sp,
                        lineHeight = 1.em
                    ),
                    maxLines = 2,
                    softWrap = true,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_RETURNS_DATE_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.quantity.toString(),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_RETURNS_STATUS_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.expiryDate.substringBefore("T"),
                    style = androidx.compose.material3.MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 11.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .weight(STOREKEEPER_RETURNS_ACTION_WEIGHT)
                    .padding(start = 4.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                IconButton(onClick = onWriteOff) {
                    Icon(
                        imageVector = Icons.Filled.DeleteForever,
                        contentDescription = "Списать партию"
                    )
                }
            }
        }
    }
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