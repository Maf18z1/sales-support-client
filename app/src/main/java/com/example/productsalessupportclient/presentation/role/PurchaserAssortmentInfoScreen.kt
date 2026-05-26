package com.example.productsalessupportclient.presentation.role

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.productsalessupportclient.data.network.AssortmentSalesPointResponse
import com.example.productsalessupportclient.data.network.ClientManagerDashboardApi
import com.example.productsalessupportclient.data.network.DashboardApi
import com.example.productsalessupportclient.data.repository.AuthSession
import com.example.productsalessupportclient.data.repository.PurchaserDashboardRepository
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextAlign
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import kotlin.math.max

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun PurchaserAssortmentInfoScreen(
    session: AuthSession,
    id: Long,
    onBack: () -> Unit
) {
    val repository = remember {
        PurchaserDashboardRepository(
            api = DashboardApi(),
            promoApi = ClientManagerDashboardApi()
        )
    }

    val vm: PurchaserAssortmentInfoViewModel = viewModel(
        factory = PurchaserAssortmentInfoViewModelFactory(repository, session.token)
    )

    val state = vm.uiState

    var chartMode by remember { mutableStateOf(ChartMode.DAY) }

    LaunchedEffect(id) {
        vm.load(id)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(12.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text("Подробная информация", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        state.error?.let {
            Text(it, color = MaterialTheme.colorScheme.error)
            Spacer(Modifier.height(8.dp))
        }

        state.item?.let { item ->
            Card(shape = MaterialTheme.shapes.large, modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp)) {
                    Text(item.name, style = MaterialTheme.typography.titleLarge)
                    Text("Категория: ${item.category ?: "—"}")
                    Text("Артикул: ${item.article ?: "—"}")
                    Text("Цена: ${item.price}")
                    Text("Остаток: ${item.stockQuantity ?: 0}")
                }
            }

            Spacer(Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("История покупок", style = MaterialTheme.typography.titleMedium)
            }

            Spacer(Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    selected = chartMode == ChartMode.DAY,
                    onClick = { chartMode = ChartMode.DAY },
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 2)
                ) {
                    Text("Дни")
                }
                SegmentedButton(
                    selected = chartMode == ChartMode.MONTH,
                    onClick = { chartMode = ChartMode.MONTH },
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 2)
                ) {
                    Text("Месяцы")
                }
            }

            Spacer(Modifier.height(8.dp))

            SalesHistoryChart(
                points = state.history,
                mode = chartMode
            )
        }

        Spacer(Modifier.height(16.dp))
        OutlinedButton(onClick = onBack) { Text("Назад") }
    }
}

private enum class ChartMode {
    DAY,
    MONTH
}

private data class ChartBucket(
    val label: String,
    val tooltipLabel: String,
    val value: Int
)

@RequiresApi(Build.VERSION_CODES.O)
@Composable
private fun SalesHistoryChart(
    points: List<AssortmentSalesPointResponse>,
    mode: ChartMode
) {
    val today = LocalDate.now()

    val buckets = remember(points, mode) {
        when (mode) {
            ChartMode.DAY -> buildDailyBuckets(points, today, daysBack = 14)
            ChartMode.MONTH -> buildMonthlyBuckets(points, today, monthsBack = 12)
        }
    }

    if (buckets.isEmpty()) {
        Text("Нет данных для графика")
        return
    }

    val maxValue = buckets.maxOf { it.value }.coerceAtLeast(1)
    var selectedBucket by remember { mutableStateOf<ChartBucket?>(buckets.lastOrNull()) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F5FF))
    ) {
        Column(modifier = Modifier.padding(12.dp)) {

            selectedBucket?.let { bucket ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Text(
                        text = "${bucket.tooltipLabel} • спрос: ${bucket.value}",
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        style = MaterialTheme.typography.labelMedium
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(36.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Спрос",
                        modifier = Modifier
                            .rotate(-90f)
                            .wrapContentSize(),
                        maxLines = 1,
                        softWrap = false,
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.Gray
                    )
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                ) {

                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .pointerInput(buckets) {
                                    detectDragGestures(
                                        onDragStart = { offset ->
                                            selectedBucket = bucketAtOffset(
                                                x = offset.x,
                                                totalWidth = size.width,
                                                buckets = buckets
                                            )
                                        },
                                        onDrag = { change, _ ->
                                            selectedBucket = bucketAtOffset(
                                                x = change.position.x,
                                                totalWidth = size.width,
                                                buckets = buckets
                                            )
                                        }
                                    )
                                }
                        ) {

                            Canvas(
                                modifier = Modifier.fillMaxSize()
                            ) {

                                val axisColor = Color(0xFFB0B0B0)

                                val stroke = 3f
                                val arrowSize = 18f

                                val bottomAxisPadding = 18f

                                val xAxisY = size.height - bottomAxisPadding

                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, xAxisY),
                                    end = Offset(0f, 0f),
                                    strokeWidth = stroke
                                )

                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(-arrowSize / 2f, arrowSize),
                                    strokeWidth = stroke
                                )

                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, 0f),
                                    end = Offset(arrowSize / 2f, arrowSize),
                                    strokeWidth = stroke
                                )

                                drawLine(
                                    color = axisColor,
                                    start = Offset(0f, xAxisY),
                                    end = Offset(size.width, xAxisY),
                                    strokeWidth = stroke
                                )

                                drawLine(
                                    color = axisColor,
                                    start = Offset(size.width, xAxisY),
                                    end = Offset(
                                        size.width - arrowSize,
                                        xAxisY - arrowSize / 2f
                                    ),
                                    strokeWidth = stroke
                                )

                                drawLine(
                                    color = axisColor,
                                    start = Offset(size.width, xAxisY),
                                    end = Offset(
                                        size.width - arrowSize,
                                        xAxisY + arrowSize / 2f
                                    ),
                                    strokeWidth = stroke
                                )
                            }

                            Row(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(
                                        start = 8.dp,
                                        bottom = 18.dp
                                    ),
                                horizontalArrangement = Arrangement.SpaceEvenly,
                                verticalAlignment = Alignment.Bottom
                            ) {

                                buckets.forEach { bucket ->

                                    val fraction =
                                        bucket.value.toFloat() / maxValue.toFloat()

                                    val barHeight = (180f * fraction)
                                        .coerceAtLeast(
                                            if (bucket.value == 0) 6f else 10f
                                        )
                                        .dp

                                    Column(
                                        modifier = Modifier
                                            .weight(1f)
                                            .padding(horizontal = 3.dp)
                                            .fillMaxHeight(),
                                        verticalArrangement = Arrangement.Bottom,
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {

                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(barHeight)
                                                .background(
                                                    color = if (selectedBucket == bucket) {
                                                        Color(0xFF5B3FE8)
                                                    } else {
                                                        Color(0xFF7C5CFF)
                                                    },
                                                    shape = RoundedCornerShape(8.dp)
                                                )
                                        )
                                    }
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "Дата",
                            style = MaterialTheme.typography.labelMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}

private fun bucketAtOffset(
    x: Float,
    totalWidth: Int,
    buckets: List<ChartBucket>
): ChartBucket? {
    if (buckets.isEmpty() || totalWidth <= 0) return null

    val index = ((x / totalWidth.toFloat()) * buckets.size)
        .toInt()
        .coerceIn(0, buckets.lastIndex)

    return buckets.getOrNull(index)
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildDailyBuckets(
    points: List<AssortmentSalesPointResponse>,
    today: LocalDate,
    daysBack: Int
): List<ChartBucket> {
    val grouped = points
        .mapNotNull { point ->
            runCatching { LocalDate.parse(point.purchaseDate) }
                .getOrNull()
                ?.let { it to point.quantity }
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { entry -> entry.value.sum() }

    val startDate = today.minusDays(daysBack - 1L)

    return (0 until daysBack).map { offset ->
        val date = startDate.plusDays(offset.toLong())
        ChartBucket(
            label = date.dayOfMonth.toString(),
            tooltipLabel = date.format(DateTimeFormatter.ofPattern("dd.MM.yyyy")),
            value = grouped[date] ?: 0
        )
    }
}

@RequiresApi(Build.VERSION_CODES.O)
private fun buildMonthlyBuckets(
    points: List<AssortmentSalesPointResponse>,
    today: LocalDate,
    monthsBack: Int
): List<ChartBucket> {
    val grouped = points
        .mapNotNull { point ->
            runCatching { LocalDate.parse(point.purchaseDate) }
                .getOrNull()
                ?.let { it.withDayOfMonth(1) to point.quantity }
        }
        .groupBy({ it.first }, { it.second })
        .mapValues { entry -> entry.value.sum() }

    val endMonth = YearMonth.from(today)
    val startMonth = endMonth.minusMonths(monthsBack - 1L)

    return (0 until monthsBack).map { offset ->
        val ym = startMonth.plusMonths(offset.toLong())
        val monthDate = ym.atDay(1)
        ChartBucket(
            label = ym.month.name.lowercase().replaceFirstChar { it.uppercase() },
            tooltipLabel = "${ym.month.name.lowercase().replaceFirstChar { it.uppercase() }} ${ym.year}",
            value = grouped[monthDate] ?: 0
        )
    }
}