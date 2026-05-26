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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.network.StorekeeperDashboardApi
import com.example.productsalessupportclient.data.network.SupplierOrderSummaryResponse
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.StorekeeperDashboardRepository

private const val TITLE_COL_WEIGHT = 2.2f
private const val QTY_COL_WEIGHT = 1.0f
private const val UPDATED_COL_WEIGHT = 1.8f

@Composable
fun StorekeeperMainScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {
    val repository = remember {
        StorekeeperDashboardRepository(
            StorekeeperDashboardApi()
        )
    }

    val vm: StorekeeperMainViewModel = viewModel(
        factory = StorekeeperMainViewModelFactory(
            repository,
            session.token
        )
    )

    val state = vm.uiState

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {
        if (state.isLoading) {
            LinearProgressIndicator(modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(12.dp))
        }

        state.error?.let {
            Text(
                text = it,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(modifier = Modifier.height(12.dp))
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            StorekeeperDashboardSection<SupplierOrderSummaryResponse>(
                modifier = Modifier.weight(1f),
                title = "Ожидаемые поступления от поставщиков",
                titleFontSize = 10.sp,
                headers = listOf("Номер", "Заказ", "Статус"),
                headerFontSize = 7.sp,
                items = state.supplierOrders,
                emptyText = "Нет поступлений"
            ) { index, item ->
                SupplierOrderRowCard(
                    index = index,
                    item = item
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                StorekeeperDashboardSection<ProductBatchResponse>(
                    modifier = Modifier.weight(1f),
                    title = "Товары к списанию",
                    titleFontSize = 10.sp,
                    headers = listOf("Товар", "Кол-во", "Срок"),
                    headerFontSize = 7.sp,
                    items = state.writeOffBatches,
                    emptyText = "Нет просроченных товаров"
                ) { index, item ->
                    BatchRowCard(
                        index = index,
                        item = item
                    )
                }

                StorekeeperDashboardSection<ClientOrderResponse>(
                    modifier = Modifier.weight(1f),
                    title = "Заказы готовые к сборке",
                    titleFontSize = 10.sp,
                    headers = listOf("Номер", "Дата", "Сумма"),
                    headerFontSize = 7.sp,
                    items = state.readyOrders,
                    emptyText = "Нет заказов"
                ) { index, item ->
                    ReadyOrderRowCard(
                        index = index,
                        item = item
                    )
                }
            }
        }
    }
}

@Composable
private fun <T> StorekeeperDashboardSection(
    modifier: Modifier = Modifier,
    title: String,
    titleFontSize: TextUnit = 12.sp,
    headers: List<String>,
    headerFontSize: TextUnit = 7.sp,
    items: List<T>,
    emptyText: String,
    rowContent: @Composable (index: Int, item: T) -> Unit
) {
    Column(
        modifier = modifier.fillMaxHeight()
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium.copy(
                fontSize = titleFontSize
            )
        )

        Spacer(modifier = Modifier.height(6.dp))

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(),
            shape = RoundedCornerShape(22.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFF8F5FF)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
            ) {
                StorekeeperHeaderRow(
                    headers = headers,
                    headerFontSize = headerFontSize
                )

                Spacer(modifier = Modifier.height(6.dp))

                if (items.isEmpty()) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = emptyText,
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontSize = 8.sp
                            ),
                            color = Color.Gray
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        itemsIndexed(items) { index, item ->
                            rowContent(index, item)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StorekeeperHeaderRow(
    headers: List<String>,
    headerFontSize: TextUnit = 7.sp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFE8E1FF),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        headers.forEachIndexed { index, header ->
            val weight = when (index) {
                0 -> TITLE_COL_WEIGHT
                1 -> QTY_COL_WEIGHT
                else -> UPDATED_COL_WEIGHT
            }

            Box(
                modifier = Modifier.weight(weight),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = header,
                    style = MaterialTheme.typography.labelMedium.copy(
                        fontSize = headerFontSize,
                        lineHeight = 0.9.em
                    ),
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
private fun SupplierOrderRowCard(
    index: Int,
    item: SupplierOrderSummaryResponse
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
                    .weight(TITLE_COL_WEIGHT)
                    .padding(end = 6.dp)
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(item.itemsList ?: "Позиции не указаны")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    Text(
                        text = item.orderNumber,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 8.sp,
                            lineHeight = 1.em
                        ),
                        maxLines = Int.MAX_VALUE,
                        softWrap = true,
                        overflow = TextOverflow.Clip
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(QTY_COL_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.orderDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 7.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = Int.MAX_VALUE,
                    softWrap = true,
                    overflow = TextOverflow.Clip
                )
            }

            Box(
                modifier = Modifier
                    .weight(UPDATED_COL_WEIGHT)
                    .padding(start = 6.dp)
            ) {
                Text(
                    text = item.status,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 7.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.Start,
                    maxLines = Int.MAX_VALUE,
                    softWrap = true,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BatchRowCard(
    index: Int,
    item: ProductBatchResponse
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
            val expiryDate = item.expiryDate.substringBefore("T")
            val expiryTime = item.expiryDate.substringAfter("T", "").substringBefore(".")

            Box(
                modifier = Modifier
                    .weight(TITLE_COL_WEIGHT)
                    .padding(end = 6.dp)
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("ID: ${item.id}\nПолучено: ${item.receivedDate}")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    Text(
                        text = item.assortmentName,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 8.sp,
                            lineHeight = 1.em
                        ),
                        maxLines = Int.MAX_VALUE,
                        softWrap = true,
                        overflow = TextOverflow.Clip
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(QTY_COL_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.quantity.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 7.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = Int.MAX_VALUE,
                    softWrap = true,
                    overflow = TextOverflow.Clip
                )
            }

            Box(
                modifier = Modifier
                    .weight(UPDATED_COL_WEIGHT)
                    .padding(start = 6.dp)
            ) {
                Text(
                    text = expiryDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 7.sp,
                        lineHeight = 1.em
                    ),
                    maxLines = Int.MAX_VALUE,
                    softWrap = true,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ReadyOrderRowCard(
    index: Int,
    item: ClientOrderResponse
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
                    .weight(TITLE_COL_WEIGHT)
                    .padding(end = 6.dp)
            ) {
                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("Статус: ${item.status}\nИсточник: ${item.source ?: "—"}")
                        }
                    },
                    state = rememberTooltipState()
                ) {
                    Text(
                        text = item.orderNumber,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontSize = 8.sp,
                            lineHeight = 1.em
                        ),
                        maxLines = Int.MAX_VALUE,
                        softWrap = true,
                        overflow = TextOverflow.Clip
                    )
                }
            }

            Box(
                modifier = Modifier
                    .weight(QTY_COL_WEIGHT)
                    .padding(horizontal = 4.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.orderDate,
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 7.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.Center,
                    maxLines = Int.MAX_VALUE,
                    softWrap = true,
                    overflow = TextOverflow.Clip
                )
            }

            Box(
                modifier = Modifier
                    .weight(UPDATED_COL_WEIGHT)
                    .padding(start = 6.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = item.totalAmount.toString(),
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontSize = 7.sp,
                        lineHeight = 1.em
                    ),
                    textAlign = TextAlign.End,
                    maxLines = Int.MAX_VALUE,
                    softWrap = true,
                    overflow = TextOverflow.Clip
                )
            }
        }
    }
}