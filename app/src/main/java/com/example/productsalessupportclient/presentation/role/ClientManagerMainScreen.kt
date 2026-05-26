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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.ClientOrderResponse
import com.example.productsalessupportclient.data.network.ProductBatchResponse
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.ClientManagerDashboardRepository

private const val TITLE_COL_WEIGHT = 2.2f
private const val QTY_COL_WEIGHT = 1.0f
private const val UPDATED_COL_WEIGHT = 1.8f

@Composable
fun ClientManagerMainScreen(
    session: AuthSession,
    onLogout: () -> Unit
) {

    val repository = remember {
        ClientManagerDashboardRepository(
            ClientManagerDashboardApi()
        )
    }

    val vm: ClientManagerMainViewModel = viewModel(
        factory = ClientManagerMainViewModelFactory(
            repository,
            session.token
        )
    )

    val state = vm.uiState

    var promoDaysInput by rememberSaveable {
        mutableStateOf("30")
    }

    val days =
        promoDaysInput.toIntOrNull() ?: 30

    LaunchedEffect(days) {
        vm.load(days)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(8.dp)
    ) {

        if (state.isLoading) {

            LinearProgressIndicator(
                modifier = Modifier.fillMaxWidth()
            )

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

            ClientManagerDashboardSection(
                modifier = Modifier.weight(1f),
                title = "Товары с истекающим сроком",
                titleFontSize = 10.sp,
                headers = listOf(
                    "Товар",
                    "Количество",
                    "Срок"
                ),
                headerFontSize = 7.sp,
                items = state.promotionProducts,
                emptyText = "Нет данных",
                emptyTextSize = 8.sp,
                titleControls = {

                    OutlinedTextField(
                        value = promoDaysInput,
                        onValueChange = {

                            if (it.all(Char::isDigit)) {
                                promoDaysInput = it
                            }
                        },
                        singleLine = true,
                        modifier = Modifier.width(72.dp),
                        textStyle = MaterialTheme.typography.bodySmall.copy(
                            fontSize = 8.sp
                        ),
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Number
                        )
                    )
                }

            ) { index, item ->

                BatchRowCard(
                    index = index,
                    item = item
                )
            }

            ClientManagerDashboardSection(
                modifier = Modifier.weight(1f),
                title = "Заказы клиентов",
                titleFontSize = 10.sp,
                headers = listOf(
                    "Номер",
                    "Дата"
                ),
                headerFontSize = 7.sp,
                items = state.pendingOrders,
                emptyText = "Нет заказов",
                emptyTextSize = 8.sp
            ) { index, item ->

                OrderRowCard(
                    index = index,
                    item = item
                )
            }
        }
    }
}

@Composable
private fun <T> ClientManagerDashboardSection(
    modifier: Modifier = Modifier,
    title: String,
    titleControls: @Composable (() -> Unit)? = null,
    titleFontSize: TextUnit = 12.sp,
    headers: List<String>,
    headerFontSize: TextUnit = 7.sp,
    items: List<T>,
    emptyText: String,
    emptyTextSize: TextUnit = 8.sp,
    rowContent: @Composable (index: Int, item: T) -> Unit
) {

    Column(
        modifier = modifier
            .fillMaxHeight()
    ) {

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {

            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = titleFontSize
                ),
                modifier = Modifier.weight(1f)
            )

            titleControls?.invoke()
        }

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

                ClientManagerHeaderRow(
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
                                fontSize = emptyTextSize
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
private fun ClientManagerHeaderRow(
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
                modifier = Modifier
                    .weight(weight),
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

            val expiryDate =
                item.expiryDate.substringBefore("T")

            val expiryTime =
                item.expiryDate
                    .substringAfter("T", "")
                    .substringBefore(".")

            Box(
                modifier = Modifier
                    .weight(TITLE_COL_WEIGHT)
                    .padding(end = 6.dp)
            ) {

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text("ID партии: ${item.id}")
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
                    textAlign = TextAlign.Center
                )
            }

            Box(
                modifier = Modifier
                    .weight(UPDATED_COL_WEIGHT)
                    .padding(start = 6.dp)
            ) {

                TooltipBox(
                    positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
                    tooltip = {
                        PlainTooltip {
                            Text(expiryTime)
                        }
                    },
                    state = rememberTooltipState()
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
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OrderRowCard(
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
                    .weight(2.6f)
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
                    .weight(1.4f)
                    .padding(start = 6.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text(
                    text = item.orderDate,
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